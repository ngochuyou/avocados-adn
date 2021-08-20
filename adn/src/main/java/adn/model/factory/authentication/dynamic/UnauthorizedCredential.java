/**
 * 
 */
package adn.model.factory.authentication.dynamic;

/**
 * @author Ngoc Huy
 *
 */
public class UnauthorizedCredential extends Exception {

	private static final long serialVersionUID = 1L;

	private String deniedCredential;

	public UnauthorizedCredential(String deniedCredential) {
		super();
		this.deniedCredential = deniedCredential;
	}

	@Override
	public String getMessage() {
		return String.format("Unauthorized credential: %s", deniedCredential);
	}

}
