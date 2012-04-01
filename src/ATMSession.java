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
		
		//The ATM tries then to authenticate the bank's server
		AuthenticationMessage msg = null;
		SignedMessage smsg = null;
		
		//First, the ATM sends it's ID and a nonce as a challenge 
		//as a plain text message
		try {
			msg = getAuthenticationMessage();
			System.out.println("First message sent");
			os.writeObject(msg);
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		//The ATM get back the server's answer and check it
		try {
			System.out.println("Waiting for the bank's response");
			msg = readAuthenticationMessage(false);
			//If the message is indeed coming from the bank
			if (msg.isSuccess()) {
				//Then the ATM can send the client's account number to
				//begin client authentication
				System.out.println("Sending account number");
				msg = getAuthenticationMessage();
				msg.setAccountNumber(card.getAcctNum());
				//Only the bank can read it
				byte[] enmsg = crypto.encryptRSA(msg, this.kBank);
				os.write(enmsg);
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
			System.out.println("Waiting for client's challenge");
			msg = readAuthenticationMessage(false);
			//If the message comes from the bank
			if (msg.isSuccess()) {
				//The ATM send a new signed message
				msg = getAuthenticationMessage();
				smsg = new SignedMessage(msg, this.kUser, crypto);
				os.writeObject(smsg);
				System.out.println("Signed message sent");
			}
			else {
				return false;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		//Finally, we get back the Signed and encrypted message
		try {
			System.out.println("waiting for shared key");
			msg = readAuthenticationMessage(true);
			//The ATM get back the shared key
			this.kSession = msg.getSessionKey();
			//The authentication is done !
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
		
			// Verify the message by checking the nonce, timestamp,
			// and signature.
			Calendar c = Calendar.getInstance();
			c.setTime(message.getTimestamp());
			c.add(Calendar.SECOND, 30);
			if (message.getAtmNonce() != this.atmNonce ||
				new Date().getTime() > c.getTimeInMillis() ||
				!crypto.verify((byte[])msg.getObject(), msg.signature, kBank))
			{
				throw new RuntimeException("Invalid message received!!!");
			}
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
	AuthenticationMessage readAuthenticationMessage(boolean rsaEncrypted)
	{
		AuthenticationMessage message = null;
		SignedMessage msg = null;
		try {
			// read the message
			msg = (SignedMessage)is.readObject();
			
			if (rsaEncrypted) {
				//This is the message encrypted with the client's public key
				message = (AuthenticationMessage) crypto.decryptRSA(msg.msg, kUser);
			}
			else {
				message = (AuthenticationMessage) msg.getObject();
			}
			
			// Verify the message by checking the nonce, timestamp,
			// and signature.
			Calendar c = Calendar.getInstance();
			c.setTime(message.getTimestamp());
			c.add(Calendar.SECOND, 30);
			
			if (message.getAtmNonce() != this.atmNonce ||
				new Date().getTime() > c.getTimeInMillis() ||
				!crypto.verify(msg.msg, msg.signature, kBank))
			{
				throw new RuntimeException("Invalid message received!!!");
			}
		}
		catch (Exception exc) {
			throw new RuntimeException(exc);
		}
		message.setSuccess(true);
		this.bankNonce = message.getBankNonce();
		return message;
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
