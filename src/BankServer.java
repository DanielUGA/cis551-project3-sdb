import java.security.*;
import java.io.*;
import java.net.*;

public class BankServer {
    
    public static final String pubKeyFile = "bank.pub";
    public static final String keyPairFile = "bank.key";
    public static final String logFile = "bank.log";
    private static KeyPair pair = null;
    public static Log log = null;
    private static Crypto crypto;
    
    static {
	try {
	    pair = (KeyPair)Disk.load(keyPairFile);
	    log = new Log(logFile, pair.getPublic());
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }


    public static void main(String[] args) {
	try {
	    AccountDB accts = AccountDB.load();
	    ServerSocket serverS = new ServerSocket(2100);
	    System.out.println("--------------------------");
	    System.out.println("  Bank Server is Running  ");
	    System.out.println("--------------------------");
	    while (true) {
		try {
		    Socket s = serverS.accept();
		    BankSession session = new BankSession(s, accts, pair);
		    new Thread(session).start();
		} catch (IOException e) {
		    log.write(e);
		}
		} 
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }
    
    public static void logViewer(Key logKey){
    	/**crypto=new Crypto(); //decrypt the log key with bank's private key and use for aes decryption
	    Key logKey=null;
			try {
				logKey = (Key) crypto.decryptRSA(log.aesKey, pair.getPrivate());
			} catch (KeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
    	log.print(logFile, logKey);
    }
}
