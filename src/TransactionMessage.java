import java.util.Date;

/**
 * Bean that is used for transmitting transaction messages
 * by both the client and the server.
 * 
 */
public class TransactionMessage implements Message {
	// Indicates the end of the session action.
	public static final int END_SESSION = 1;
	// Indicates a deposit action.
	public static final int DEPOSIT = 2;
	// Indicates a withdrawal action.
	public static final int WITHDRAWAL = 3;
	// Indicates a balance request action.
	public static final int BALANCE = 4;
	
	private static final long serialVersionUID = 1;
	private int action;
	private boolean success;
	private double amount;
	private int atmNonce;
	private int bankNonce;
	private Date timestamp;
	public int getAction() {
		return action;
	}
	public void setAction(int action) {
		this.action = action;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
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
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
}
