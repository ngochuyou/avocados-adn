/**
 * 
 */
package adn.model.factory.authentication;

/**
 * Credentials that are hard coded into Enums
 * 
 * @author Ngoc Huy
 *
 */
public interface EnumeratedCredential extends Credential {

	/**
	 * @return the type of the enum credential
	 */
	Class<? extends Enum<?>> getEnumtype();
	
}
