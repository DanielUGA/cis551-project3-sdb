import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Message that will be used for logging.
 */
public class LogMessage implements Message {
	private static final long serialVersionUID = 1;
	private Date timestamp;
	private int action;
	private boolean successful;
	private double amount;
	private String acctNumber;
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public int getAction() {
		return action;
	}
	public void setAction(int action) {
		this.action = action;
	}
	public boolean isSuccessful() {
		return successful;
	}
	public void setSuccessful(boolean successful) {
		this.successful = successful;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getAcctNumber() {
		return acctNumber;
	}
	public void setAcctNumber(String acctNumber) {
		this.acctNumber = acctNumber;
	}
	@Override
	public String toString() {
		// Set the timestamp.
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
		StringBuilder sb = new StringBuilder(format.format(this.timestamp));
		sb.append(" "+acctNumber);
		sb.append(successful ? " SUCCESS " : " FAILURE ");
		switch (action)
		{
		case TransactionMessage.BALANCE:
			sb.append("Requested Balance was: "+amount);
			break;
		case TransactionMessage.DEPOSIT:
			sb.append("Deposited Amount was: "+amount);
			break;
		case TransactionMessage.END_SESSION:
			sb.append("User ended session.");
			break;
		case TransactionMessage.WITHDRAWAL:
			sb.append("Withdrawal Amount was: " +amount);
		}
		return sb.toString();
	}
}
