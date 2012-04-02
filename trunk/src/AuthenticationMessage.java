
/**
 * Bean that is used for transmitting messages during the
 * authentication phase by both the client and the server.
 */
public class AuthenticationMessage extends GeneralMessage {

	private static final long serialVersionUID = 5709932797618707625L;
	private String atmID;
	private byte[] sessionKey;
	private byte[] accountNumber;
	
	public byte[] getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(byte[] accountNumber) {
		this.accountNumber = accountNumber;
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
}
