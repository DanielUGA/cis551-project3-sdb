import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Date;


public class AuthenticationLogMessage implements Message {

	private static final long serialVersionUID = -1115876731158043215L;
	private String atmID;
	private Date timestamp;
	private String message;
	private Key skey;
	private String acctNumber;
	
	public AuthenticationLogMessage(Date timestamp, String atmID, 
									String message)
	{
		this.atmID = atmID;
		this.message = message;
		this.timestamp = timestamp;
	}
	public AuthenticationLogMessage(Date timestamp, String atmID, 
			String message, Key skey, String acctNumber)
	{
		this.atmID = atmID;
		this.message = message;
		this.timestamp = timestamp;
		this.skey = skey;
		this.acctNumber = acctNumber;
	}
	public Key getSkey() {
		return skey;
	}
	public void setSkey(Key skey) {
		this.skey = skey;
	}
	public String getAtmID() {
		return atmID;
	}
	public void setAtmID(String atmID) {
		this.atmID = atmID;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getAcctNumber() {
		return acctNumber;
	}
	public void setAcctNumber(String acctNumber) {
		this.acctNumber = acctNumber;
	}
	
	@Override
	public String toString() {
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
		StringBuilder sb = new StringBuilder(format.format(this.timestamp));
		sb.append(" ATM #"+ atmID);
		sb.append(": "+message);
		return sb.toString();
	}
}
