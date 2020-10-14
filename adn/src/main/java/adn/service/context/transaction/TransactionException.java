/**
 * 
 */
package adn.service.context.transaction;

/**
 * @author Ngoc Huy
 *
 */
public class TransactionException extends Exception {

	private String message;

	private String transactionId;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TransactionException(String message, String transactionId) {
		this.message = message;
		this.transactionId = transactionId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

}
