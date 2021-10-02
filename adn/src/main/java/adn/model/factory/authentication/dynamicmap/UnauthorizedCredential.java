/**
 * 
 */
package adn.model.factory.authentication.dynamicmap;

/**
 * @author Ngoc Huy
 *
 */
public class UnauthorizedCredential extends Exception {

	private static final long serialVersionUID = 1L;

	private static final String UNKNOWN_RESOURCE = "unknown resource";

	private final String message;

	public UnauthorizedCredential(String deniedCredential) {
		this(deniedCredential, UNKNOWN_RESOURCE);
	}

	public UnauthorizedCredential(String deniedCredential, String resourceName) {
		super();
		message = String.format("Unauthorized credential: %s on %s", deniedCredential, resourceName);
	}

	@Override
	public String getMessage() {
		return message;
	}

}
