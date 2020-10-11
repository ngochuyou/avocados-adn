package adn.service.transaction;

public class TransactionException extends Exception {

	private String message;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TransactionException(String message) {
		// TODO Auto-generated constructor stub
		this.message = message;
	}

	@Override
	public String getMessage() {
		// TODO Auto-generated method stub
		return message;
	}
}
