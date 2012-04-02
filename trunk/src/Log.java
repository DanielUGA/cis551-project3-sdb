import java.security.Key;
import java.security.KeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Log {

	private Crypto crypto;
	private PublicKey key;
	private FileOutputStream outputStream;
	public byte[] aesKey;

	// You may add more state here.
	private Key logKey;

	public Log(String file, PublicKey key) 
	{
		try {
			this.crypto = new Crypto();
			//this.key = key;	
		    logKey = crypto.makeAESKey();
			this.aesKey = crypto.encryptRSA(logKey, key);
			outputStream = new FileOutputStream(file);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void write(Serializable obj) {
		System.out.println("writing to log "+obj.toString());
		try {
			outputStream= new FileOutputStream(BankServer.logFile, true);
			ObjectOutputStream oos = new ObjectOutputStream(outputStream);
			oos.writeObject(crypto.encryptAES(obj.toString().getBytes(), logKey));
			oos.close();
			outputStream.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void print(Key logKey) {
		FileInputStream inputStream = null;
		ObjectInputStream ois = null;
		//decrypt the contents of the log file
		//boolean isEndofFile = false;
		try {
			inputStream =new FileInputStream(BankServer.logFile);
			ois = new ObjectInputStream(inputStream);
			Object obj = null;
				while((obj = ois.readObject()) != null) {
					obj=ois.readObject();
					System.out.println(crypto.decryptAES(((LogMessage)obj).toString().getBytes(), logKey));
			}
		}catch (EOFException ex) { //This exception will be caught when EOF is reached
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
		} finally {
			//Close the ObjectInputStream
			try {
				if (ois != null) {
					ois.close();				
					inputStream.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
