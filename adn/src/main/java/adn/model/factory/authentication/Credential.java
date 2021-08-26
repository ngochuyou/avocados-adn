/**
 * 
 */
package adn.model.factory.authentication;

/**
 * Represent anything that forms a complete credential in order to access
 * specific resources
 * 
 * @author Ngoc Huy
 *
 */
public interface Credential {

	/**
	 * @return the credential value as a {@code String}
	 */
	String evaluate();

	/**
	 * @return the position of the credential in a compound credential
	 */
	int getPosition();
	
	default boolean equal(Credential other) {
		return this.evaluate().equals(other.evaluate());
	}

}
