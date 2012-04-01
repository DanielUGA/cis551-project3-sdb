import java.security.Key;
import java.util.Date;


/**
 * Bean that is used for transmitting messages during the
 * authentication phase by both the client and the server.
 */
public class AuthenticationMessage implements Message {

	private static final long serialVersionUID = 1;
	private String atmID;
	private int atmNonce;
	private int bankNonce;
	private Date timestamp;
	private byte[] sessionKey;
	private boolean Success;
	private byte[] accountNumber;
	
	public byte[] getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(byte[] accountNumber) {
		this.accountNumber = accountNumber;
	}
	public boolean isSuccess() {
		return Success;
	}
	public void setSuccess(boolean success) {
		Success = success;
	}
	public String getAtmID() {
		return atmID;
	}
	public void setAtmID(String atmID) {
		this.atmID = atmID;
	}
	public byte[] getSessionKey() {
		return sessionKey;
	}
	public void setSessionKey(byte[] sessionKey) {
		this.sessionKey = sessionKey;
	}
	public int getAtmNonce() {
		return atmNonce;
	}
	public void setAtmNonce(int atmNonce) {
		this.atmNonce = atmNonce;
	}
	public int getBankNonce() {
		return bankNonce;
	}
	public void setBankNonce(int bankNonce) {
		this.bankNonce = bankNonce;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
