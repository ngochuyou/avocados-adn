/**
 * 
 */
package adn.controller.exception;

/**
 * @author Ngoc Huy
 *
 */
public class UnauthorisedDepartmentException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String message;

	public UnauthorisedDepartmentException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}

}
