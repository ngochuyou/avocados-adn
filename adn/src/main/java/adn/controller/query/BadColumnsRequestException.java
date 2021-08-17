/**
 * 
 */
package adn.controller.query;

/**
 * Indicates columns request from a {@code ServletRequest} fails to satisfy some
 * querying logics
 * 
 * @author Ngoc Huy
 *
 */
public class BadColumnsRequestException extends Exception {

	private static final long serialVersionUID = 2787129904623875749L;

	private String message;

	public BadColumnsRequestException() {
		super();
	}

	public BadColumnsRequestException(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
