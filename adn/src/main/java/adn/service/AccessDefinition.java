/**
 * 
 */
package adn.service;

/**
 * @author Ngoc Huy
 *
 */
public interface AccessDefinition {

	boolean canModify(Role request);

	boolean canRead(Role requested);

}
