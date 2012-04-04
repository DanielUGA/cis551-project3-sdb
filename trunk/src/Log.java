import java.security.Key;
import java.security.KeyException;
import java.security.KeyPair;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Log {

	private Crypto crypto;

	// You may add more state here.
	private Key logKey;			
    public static final String logkeyFile = "log.key";	
    private ObjectOutputStream oos;
    private FileOutputStream outputStream;
    
    //generates an AES key for encrypting the log contents
    //the key is encrypted with the bank's public key and saved in the log.key file
	public Log(String file, KeyPair keyPair) 
	{
		try {
			this.crypto = new Crypto();
		    
			initLogKey(keyPair);
			
			
			// Create the OutputStream, always overwrite the file
			// so that the log file can be read in correctly.  If the server
			// shuts down and you want to keep the file, move the old one to
			// a safe location and keep the key file that can be used to unlock
			// it.
			FileOutputStream outputStream = null;
			outputStream= new FileOutputStream(file, false);
			oos = new ObjectOutputStream(outputStream);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Loads the log AES key from a file if it exists, otherwise
	 * creates a new one and writes it to the secure RAM.
	 * 
	 * @param key
	 * @throws Exception
	 */
	private void initLogKey(KeyPair key) throws Exception
	{
		try {
			// use the saved log key if it exists.
			byte[] bytes = (byte[])Disk.load(Log.logkeyFile);
			logKey = (Key)crypto.decryptRSA(bytes, key.getPrivate());
		}
		catch(Exception exc)
		{
			// if it does not, then create a new one and save it.
			logKey = crypto.makeAESKey();
			byte[] aesKey = crypto.encryptRSA(logKey, key.getPublic());
		    Disk.save(aesKey, logkeyFile);
		}
	}
	
	/**
	 * Close out the streams.  Should be called when the bank server
	 * shuts down.
	 * 
	 * @throws Exception
	 */
	public void close() throws Exception
	{

		oos.close();
		outputStream.close();
	}
	
	/**
	 * Append to the log file.
	 * 
	 * @param obj
	 */
	public void write(Serializable obj) {
		System.out.println(obj.toString());
		try {
			oos.writeObject(crypto.encryptAES(obj, logKey));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Iterates through the log file printing out the messages.
	 * 
	 * @param crypto
	 * @param logKey
	 */
	public static void print(Crypto crypto, Key logKey) {
		FileInputStream inputStream = null;
		ObjectInputStream ois = null;

		try {
			inputStream =new FileInputStream(BankServer.logFile);
			ois = new ObjectInputStream(inputStream);
			Object obj = null;
			
			while((obj = ois.readObject()) != null) {
				obj=crypto.decryptAES((byte[])obj, logKey);
				System.out.println(obj);
			}
			
		} 
		catch (EOFException ex) { 
			//This exception will be caught when EOF is reached
	        System.out.println("End of file reached.");
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		catch (KeyException e) {
			e.printStackTrace();
		} 
		finally {
			//Close the ObjectInputStream
			try {
				if (ois != null) {
					ois.close();				
					inputStream.close();
				}
			} 
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	public static void main(String[] args) throws Exception
	{
		// Load and decrypt the log key.
		Crypto crypto = new Crypto();
		byte[] bytes = (byte[])Disk.load(Log.logkeyFile);
		KeyPair pair = (KeyPair)Disk.load(BankServer.keyPairFile);
		
		// Print out the file.
		Log.print(crypto, (Key)crypto.decryptRSA(bytes, pair.getPrivate()));
	}
}
