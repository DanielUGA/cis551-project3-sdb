import java.security.PublicKey;
import java.io.FileOutputStream;
import java.io.Serializable;

public class Log {

    private Crypto crypto;
    private FileOutputStream outputStream;
    private PublicKey key;
    
    // You may add more state here.

    public Log(String file, PublicKey key) 
    {
	try {
	    this.crypto = new Crypto();
	    this.key = key;
	    outputStream = new FileOutputStream(file);

	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    public void write(Serializable obj) {
    	System.out.println(obj.toString());
    	try {
    		outputStream.write(crypto.encryptAES(obj.toString().getBytes(), key));
    	} catch (Exception e)
    	{
    		e.printStackTrace();
    	}
    }

}
