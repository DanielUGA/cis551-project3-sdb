import java.util.Date;

/**
 * Represents the common attributes between AuthenticationMessage
 * and TransactionMessage.
 */
public abstract class GeneralMessage implements Message {

	private static final long serialVersionUID = 7627475045050904746L;
	private boolean success;
	private int atmNonce;
	private int bankNonce;
	private Date timestamp;
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
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
