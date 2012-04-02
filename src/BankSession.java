import java.net.*;
import java.io.*;
import java.security.*;
import java.util.Calendar;
import java.util.Date;

public class BankSession implements Session, Runnable {
    private Socket s;
    private ObjectOutputStream os;
    private ObjectInputStream is;

    private AccountDB accts;
    private Crypto crypto;
    private PrivateKey kPrivBank;
    private PublicKey  kPubBank;

    // These fields are initialized during authentication
    private Key kSession;
    private Account currAcct;
    private String atmID;
    private String atmName;

    // Add additional fields you need here
    private int atmNonce;
    private int bankNonce;

    BankSession(Socket s, AccountDB a, KeyPair p)
	throws IOException
    {
	this.s = s;
	OutputStream out =  s.getOutputStream();
	this.os = new ObjectOutputStream(out);
	InputStream in = s.getInputStream();
	this.is = new ObjectInputStream(in);
	this.accts = a;
	this.kPrivBank = p.getPrivate();
	this.kPubBank = p.getPublic();
	this.crypto = new Crypto();
    }

    public void run() {
	try {
	    if (authenticateUser()) {
		while (doTransaction()) {
		    // loop
		}
	    }
	    is.close();
	    os.close();
	} 
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    // Interacts with an ATMclient to 
    // (1) Authenticate the user
    // (2) If the user is valid, establish session key and any
    //     additional information needed for the protocol.
    // (3) Maintain a log of whether the login attempt succeeded
    // (4) Returns true if the user authentication succeeds, false otherwise
    
    public boolean authenticateUser() {

	// The server waits for the ATM's message, and then check
    // the account number
    AuthenticationMessage msg = null;
    SignedMessage smsg = null;
    	try {
    		//Receiving the atmID and the encrypted account number
    		System.out.println("Waiting for first message");
    		msg = (AuthenticationMessage) is.readObject();
    		System.out.println("First message from ATM #"+msg.getAtmID());
    		this.atmID = msg.getAtmID();
    		this.atmNonce = msg.getAtmNonce();
    		
    		//The bank get back the account number and account name
    		this.currAcct=this.accts.getAccount((String)crypto.decryptRSA(msg.getAccountNumber(),kPrivBank));
    		this.atmName = this.currAcct.getOwner();
    		//The bank then send a challenge to verify identity
    		//(sign it)
    		msg = getAuthenticationMessage();
    		smsg = new SignedMessage (msg, this.kPrivBank, crypto);
    		
    		os.writeObject(smsg);
    		System.out.println("Signed Challenge sent");
    		
		} catch (Exception exc) {
			exc.printStackTrace();
			return false;
		}
    	
    //Then, the bank now wait for the answer
    	try {
			System.out.println("Waiting for challenge's response");
			//Get back the signed message, and verify it 
			msg = readChallengeClient();
			if (msg.isSuccess()) {
				System.out.println("response received");
				//Then the bank can now send the shared AES key
				msg = getAuthenticationMessage();
				this.kSession = crypto.makeAESKey();
				msg.setSessionKey(crypto.encryptRSA(kSession, currAcct.kPub));
				//Sign the message
				smsg = new SignedMessage (msg, this.kPrivBank, crypto);
				
				System.out.println("Sending shared key");
				os.writeObject(smsg);
				System.out.println("Authentication over");
			}
			else {
				return false;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
    	  
	return true;
    }

    /**
     * Transmits a transaction message to the ATM machine.
     * 
     * @param message
     */
    void transmitTransactionMessage(TransactionMessage message)
	{
		try
		{
			SignedMessage msg = new SignedMessage(
			crypto.encryptAES(message,this.kSession), this.kPrivBank, crypto);
			
			os.writeObject(msg);
		} 
		catch (Exception exc)
		{
			throw new RuntimeException (exc);
		}
	}
	
    /**
     * @return Reads, verifies, and returns a TransactionMessage.
     */
	TransactionMessage readTransactionMessage()
	{
		TransactionMessage message = null;
		SignedMessage msg = null;
		try {
			// read the message
			msg = (SignedMessage)is.readObject();
			
			// decrypt the TransactionMessage.
			message = (TransactionMessage)
					crypto.decryptAES((byte[])msg.getObject(), kSession);
		
			// Verify the message by checking the nonce, timestamp,
			// and signature.
			Calendar c = Calendar.getInstance();
			c.setTime(message.getTimestamp());
			c.add(Calendar.SECOND, 30);
			if (message.getBankNonce() != this.bankNonce ||
				new Date().getTime() > c.getTimeInMillis() ||
				!crypto.verify(msg.msg, msg.signature, 
												this.currAcct.getKey()))
			{
				throw new RuntimeException("Invalid message received!!!");
			}
		}
		catch (Exception exc) {
			throw new RuntimeException(exc);
		}
		// save the atm nonce so that it can be returned for the
		// next message.
		this.atmNonce = message.getAtmNonce();
		return message;
	}
	
	/**
     * @return Reads, verifies, and returns the challenge
     * sent by the bank and returned by the client (by the ATM)
     */
	AuthenticationMessage readChallengeClient()
	{
		AuthenticationMessage message = null;
		SignedMessage msg = null;
		try {
			// read the message
			msg = (SignedMessage)is.readObject();
			
			// get back the AuthenticationMessage.
			message = (AuthenticationMessage) msg.getObject();
		
			// Verify the message by checking the nonce, timestamp,
			// and signature.
			Calendar c = Calendar.getInstance();
			c.setTime(message.getTimestamp());
			c.add(Calendar.SECOND, 30);
			if (message.getBankNonce() != this.bankNonce ||
				new Date().getTime() > c.getTimeInMillis() || 
				!crypto.verify(msg.msg, msg.signature, 
												this.currAcct.getKey()))
			{
				throw new RuntimeException("Invalid message received!!!");
			}
		}
		catch (Exception exc) {
			throw new RuntimeException(exc);
		}
		// save the atm nonce so that it can be returned for the
		// next message.
		this.atmNonce = message.getAtmNonce();
		message.setSuccess(true);
		return message;
	}
	
	/**
	 * @return Creates and returns a TransactionMessage containing the
	 * default data.
	 */
	TransactionMessage getTransactionMessage()
	{
		TransactionMessage message = new TransactionMessage();
		message.setBankNonce(getBankNonce());
		message.setAtmNonce(this.atmNonce);
		message.setTimestamp(new Date());
		
		return message;
	}
	
	/**
	 * Creates and returns an AuthenticationMessage containing default data.
	 */
	AuthenticationMessage getAuthenticationMessage()
	{
		AuthenticationMessage message = new AuthenticationMessage();
		message.setAtmID(this.atmID);
		message.setAtmNonce(this.atmNonce);
		message.setBankNonce(getBankNonce());
		message.setTimestamp(new Date());
		message.setAccountName(this.atmName);
		
		return message;	
	}
    
	/**
	 * @return Creates a returns a nonce that will be used to verify
	 * messages from the client.
	 */
	int getBankNonce()
	{
		this.bankNonce = (int) (Math.random() * Integer.MAX_VALUE);
		return bankNonce;
	}
    
    // Interacts with an ATMclient to 
    // (1) Perform a transaction 
    // (2) or end transactions if end-of-session message is received
    // (3) Maintain a log of the information exchanged with the client
    public boolean doTransaction() {
    	// Get the transaction message sent by the ATM.
    	TransactionMessage message = readTransactionMessage();
    	
    	// Prepare the transaction response message.
    	TransactionMessage response = getTransactionMessage();
    	boolean result = false;
    	synchronized (this.currAcct.getNumber().intern())
    	{
    		switch (message.getAction())
    		{
    		case TransactionMessage.BALANCE:
    			response.setAmount(this.currAcct.getBalance());
    			response.setSuccess(true);
    			// add the amount to the received message so that
    			// it can be logged.
    			message.setAmount(response.getAmount());
    			BankServer.log.write(getLogMessage(message, true));
    			result = true;
    			break;
    		case TransactionMessage.DEPOSIT:
    			currAcct.deposit(message.getAmount());
    			response.setAmount(this.currAcct.getBalance());
    			response.setSuccess(true);
    			BankServer.log.write(getLogMessage(message, true));
    			
    			result = true;
    			break;
    		case TransactionMessage.WITHDRAWAL:
    			try {
    				currAcct.withdraw(message.getAmount());
        			response.setAmount(this.currAcct.getBalance());
    				response.setSuccess(true);
        			BankServer.log.write(getLogMessage(message, true));
    			} 
    			catch (Exception exc) {
    				response.setSuccess(false);
        			BankServer.log.write(getLogMessage(message, false));
    			}
    			result = true;
    			break;
    		case TransactionMessage.END_SESSION:
    			response.setSuccess(true);
    			BankServer.log.write(getLogMessage(message, true));
    		}
    	}
    	transmitTransactionMessage(response);
    	return result;
    }
    
    /**
     * Creates a log message for the logger.
     * 
     * @param message
     * @param success
     * @return
     */
    LogMessage getLogMessage(TransactionMessage message, boolean success)
    {
    	LogMessage msg = new LogMessage();
    	msg.setAction(message.getAction());
    	msg.setAmount(message.getAmount());
    	msg.setSuccessful(success);
    	msg.setTimestamp(new Date());
    	msg.setAcctNumber(this.currAcct.getNumber());
    	return msg;
    }
}

