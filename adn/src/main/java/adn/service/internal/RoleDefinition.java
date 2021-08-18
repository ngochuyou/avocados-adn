/**
 * 
 */
package adn.service.internal;

/**
 * @author Ngoc Huy
 *
 */
public interface RoleDefinition {

	boolean canModify(Role request);

	boolean canRead(Role requested);

	boolean canBeUpdatedTo(Role requested);

}
