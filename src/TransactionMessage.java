

/**
 * Bean that is used for transmitting transaction messages
 * by both the client and the server.
 * 
 */
public class TransactionMessage extends GeneralMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4177231744583239641L;
	// Indicates the end of the session action.
	public static final int END_SESSION = 1;
	// Indicates a deposit action.
	public static final int DEPOSIT = 2;
	// Indicates a withdrawal action.
	public static final int WITHDRAWAL = 3;
	// Indicates a balance request action.
	public static final int BALANCE = 4;
	
	private int action;
	private double amount;

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
	
}
