import java.io.*;
import java.security.*;
import java.util.Calendar;
import java.util.Date;
import java.net.*;

public class ATMSession implements Session {
	private Socket s;
	private ObjectOutputStream os;
	private ObjectInputStream is;
	private BufferedReader textIn;

	private String ID;
	private ATMCard card;
	private PublicKey kBank;
	private PrivateKey kUser;
	private Crypto crypto;

	// This field is initialized during authentication
	private Key kSession;

	// Additional fields here
	private int atmNonce;
	private int bankNonce;

	ATMSession(Socket s, String ID, ATMCard card, PublicKey kBank) {
		this.s = s;
		this.ID = ID;
		this.card = card;
		this.kBank = kBank;
		this.crypto = new Crypto();
		try {
			textIn = new BufferedReader(new InputStreamReader(System.in));
			OutputStream out = s.getOutputStream();
			this.os = new ObjectOutputStream(out);
			InputStream in = s.getInputStream();
			this.is = new ObjectInputStream(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// This method authenticates the user and establishes a session key.
	public boolean authenticateUser() {
	System.out.println("Please enter your PIN: ");
	
		// First, the smartcard checks the user's pin to get the 
		// user's private key.
		try {
		    String pin = textIn.readLine();
		    kUser = card.getKey(pin);
		} catch (Exception e) {
		    return false;
		}
		
		//The ATM tries first then to authenticate the client
		AuthenticationMessage msg = null;
		SignedMessage smsg = null;
		
		//First, the ATM sends it's ID and the client's account
		//number encrypted with the bank's public key
		try {
			msg = getAuthenticationMessage();
			msg.setAccountNumber(crypto.encryptRSA(card.getAcctNum(),kBank));
			System.out.println("Authentication init...");
			
			os.writeObject(msg);
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		//The ATM get back the server's challenge
		try {
			System.out.println("Waiting for the bank's response");
			msg = readAuthenticationMessage();
			//If the message is indeed coming from the bank
			if (msg.isSuccess()) {
				System.out.println("Challenge received");
				msg = getAuthenticationMessage();
				//We sign it with the client's private key
				smsg = new SignedMessage(msg, this.kUser, crypto);
				
				System.out.println("Response sent");
				os.writeObject(smsg);
			}
			else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		//Now we get the challenge for the client's authentication
		try {
			System.out.println("Waiting for the shared key");
			msg = readAuthenticationMessage();
			//If the message comes from the bank
			if (msg.isSuccess()) {
				System.out.println("Received");
				//The ATM get back the shared AES key
				this.kSession = (Key)crypto.decryptRSA(msg.getSessionKey(), kUser);
				System.out.println("Shared key obtained");
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

	void printMenu() {
		System.out.println("*****************************");
		System.out.println("(1) Deposit");
		System.out.println("(2) Withdraw");
		System.out.println("(3) Get Balance");
		System.out.println("(4) Quit\n");
		System.out.print("Please enter your selection: ");
	}

	int getSelection() {
		try {
			String s = textIn.readLine();
			int i = Integer.parseInt(s, 10);
			return i;
		} catch (IOException e) {
			return -1;
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	double getDouble() {
		try {
			String s = textIn.readLine();
			double d = Double.parseDouble(s);
			return d;
		} catch (IOException e) {
			return 0.0;
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}

	/**
	 * Ends the session.
	 */
	void endSession() {
		TransactionMessage message = getTransactionMessage();
		message.setAction(TransactionMessage.END_SESSION);
		transmitTransactionMessage(message);
		
		TransactionMessage response = readTransactionMessage();
		if (!response.isSuccess()) {
			throw new RuntimeException("Failed to log out.");
		}
	}

	/**
	 * Handles a deposit.
	 */
	void doDeposit() {
		System.out.println("Enter the deposit amount: ");
		TransactionMessage message = getTransactionMessage();
		message.setAction(TransactionMessage.DEPOSIT);
		message.setAmount(getDouble());
		// The amount must be greater than zero, if it is not,
		// print an error message.
		if (message.getAmount() > 0)
		{
			transmitTransactionMessage(message);
			TransactionMessage response = readTransactionMessage();
			if (response.isSuccess())
			{
				System.out.println("Deposit was successful.");
				System.out.println("New Balance: "+response.getAmount());
			}
			else {
				System.out.println("Deposit failed");
			}
		}
		else
		{
			System.out.println("Entry was invalid.");
		}
	}

	/**
	 * Handles a withdrawal.
	 */
	void doWithdrawal() {
		System.out.println("Enter the withdrawal amount: ");
		TransactionMessage message = getTransactionMessage();
		message.setAction(TransactionMessage.WITHDRAWAL);
		message.setAmount(getDouble());
		
		// Amount must be greater than zero.  If it is not, print
		// an error message.
		if (message.getAmount() > 0)
		{
			transmitTransactionMessage(message);
			TransactionMessage response = readTransactionMessage();
			if (response.isSuccess())
			{
				System.out.println("Withdrawal was successful.");
				System.out.println("New Balance: "+response.getAmount());
			}
			else {
				System.out.println("Withdrawal failed.");
			}
		}
		else
		{
			System.out.println("Entry was invalid.");
		}
	}

	/**
	 * Requests a balance.
	 */
	void doBalance() {
		TransactionMessage message = getTransactionMessage();
		message.setAction(TransactionMessage.BALANCE);
		
		transmitTransactionMessage(message);
		TransactionMessage response = readTransactionMessage();
		if (response.isSuccess())
		{
			System.out.println("Balance: "+response.getAmount());
		}
		else {
			System.out.println("Balance could not be retrieved.");
		}
	}
	
	/**
	 * Transmits an encrypted and signed Transaction Message to the bank.
	 * 
	 * @param message
	 */
	void transmitTransactionMessage(TransactionMessage message)
	{
		try
		{
			SignedMessage msg = new SignedMessage(
			crypto.encryptAES(message,this.kSession), this.kUser, crypto);
			
			os.writeObject(msg);
		} 
		catch (Exception exc)
		{
			throw new RuntimeException (exc);
		}
	}
	
	/**
	 * Reads, verifies, and returns a transaction message received from the
	 * bank server.
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
		
			if (!doBasicTests(message, msg)) {
				throw new RuntimeException("Invalid message received!!!");
			}	
//			// Verify the message by checking the nonce, timestamp,
//			// and signature.
//			Calendar c = Calendar.getInstance();
//			c.setTime(message.getTimestamp());
//			c.add(Calendar.SECOND, 30);
//			if (message.getAtmNonce() != this.atmNonce ||
//				new Date().getTime() > c.getTimeInMillis() ||
//				!crypto.verify(msg.msg, msg.signature, kBank))
//			{
//				throw new RuntimeException("Invalid message received!!!");
//			}
		}
		catch (Exception exc) {
			throw new RuntimeException(exc);
		}
		this.bankNonce = message.getBankNonce();
		return message;
	}
	
	/**
	 * Reads, verifies, and returns a transaction message received from the
	 * bank server.
	 */
	AuthenticationMessage readAuthenticationMessage()
	{
		AuthenticationMessage message = null;
		SignedMessage msg = null;
		try {
			// read the message
			msg = (SignedMessage)is.readObject();
			
			message = (AuthenticationMessage) msg.getObject();
			
			
			if (!doBasicTests(message, msg)) {
				throw new RuntimeException("Invalid message received!!!");
			}		
//			// Verify the message by checking the nonce, timestamp,
//			// and signature.
//			Calendar c = Calendar.getInstance();
//			c.setTime(message.getTimestamp());
//			c.add(Calendar.SECOND, 30);
//			
//			if (message.getAtmNonce() != this.atmNonce ||
//				new Date().getTime() > c.getTimeInMillis() ||
//				!crypto.verify(msg.msg, msg.signature, kBank))
//			{
//				throw new RuntimeException("Invalid message received!!!");
//			}
		}
		catch (Exception exc) {
			throw new RuntimeException(exc);
		}
		message.setSuccess(true);
		this.bankNonce = message.getBankNonce();
		return message;
	}
	
	/**
	 * Check the nonce, the timestamp and the signature
	 */
	boolean doBasicTests(GeneralMessage message, SignedMessage msg) {
		
		Calendar c = Calendar.getInstance();
		c.setTime(message.getTimestamp());
		c.add(Calendar.SECOND, 30);
		
		try {
			if (message.getAtmNonce() != this.atmNonce ||
					new Date().getTime() > c.getTimeInMillis() ||
					!crypto.verify(msg.msg, msg.signature, kBank))
			{
				return false;
			}
			else {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Creates and returns a TransactionMessage containing default data.
	 */
	TransactionMessage getTransactionMessage()
	{
		TransactionMessage message = new TransactionMessage();
		message.setAtmNonce(getATMNonce());
		message.setBankNonce(this.bankNonce);
		message.setTimestamp(new Date());
		
		return message;
	}
	
	/**
	 * Creates and returns an AuthenticationMessage containing default data.
	 */
	AuthenticationMessage getAuthenticationMessage()
	{
		AuthenticationMessage message = new AuthenticationMessage();
		message.setAtmID(this.ID);
		message.setAtmNonce(getATMNonce());
		message.setBankNonce(this.bankNonce);
		message.setTimestamp(new Date());
		
		return message;	
	}
	
	/**
	 * Creates and returns a nonce that will be used to verify 
	 * messages from the bank server.
	 */
	int getATMNonce()
	{
		this.atmNonce = (int) (Math.random() * Integer.MAX_VALUE);
		return atmNonce;
	}

	public boolean doTransaction() {
		printMenu();
		int x = getSelection();
		switch (x) {
		case 1:
			doDeposit();
			break;
		case 2:
			doWithdrawal();
			break;
		case 3:
			doBalance();
			break;
		case 4: {
			endSession();
			return false;
		}
		default: {
			System.out.println("Invalid choice.  Please try again.");
		}
		}
		return true;
	}
}
