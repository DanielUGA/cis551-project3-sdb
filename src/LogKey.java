import java.security.Key;


public class LogKey {
    private static Crypto crypto = new Crypto();

    public static void main(String[] args) {
	try {
	    Key kp = crypto.makeAESKey();
	    Disk.save(kp, Log.keyFile);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
