import java.security.Key;
import java.util.Date;


public class AuthenticationLogMessage implements Message {

	private static final long serialVersionUID = -1115876731158043215L;
	private String accountNumber;
	private String atmID;
	private Date timestamp;
	private String steps;
	private Key skey;
	
	public Key getSkey() {
		return skey;
	}
	public void setSkey(Key skey) {
		this.skey = skey;
	}
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
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
	public String getSteps() {
		return steps;
	}
	public void setSteps(String steps) {
		this.steps = steps;
	}
	

}
