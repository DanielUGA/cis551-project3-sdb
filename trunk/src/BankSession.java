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

	// replace this with the appropriate code
	return false;
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
				!crypto.verify((byte[])msg.getObject(), msg.signature, 
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
    			response.setSuccess(true);
    			BankServer.log.write(getLogMessage(message, true));
    			
    			result = true;
    			break;
    		case TransactionMessage.WITHDRAWAL:
    			try {
    				currAcct.withdraw(message.getAmount());
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
    			BankServer.log.write(getLogMessage(message, false));
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
    	msg.setAcctNumber(this.currAcct.getNumber());
    	return msg;
    }
}

