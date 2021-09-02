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

	private final String deniedCredential;
	private final String resourceName;

	private static final String UNKNOWN_RESOURCE = "Resource name not given";

	public UnauthorizedCredential(String deniedCredential) {
		super();
		this.deniedCredential = deniedCredential;
		resourceName = UNKNOWN_RESOURCE;
	}

	public UnauthorizedCredential(String deniedCredential, String resourceName) {
		super();
		this.deniedCredential = deniedCredential;
		this.resourceName = resourceName;
	}

	@Override
	public String getMessage() {
		return String.format("Unauthorized credential: %s on %s", deniedCredential, resourceName);
	}

}
