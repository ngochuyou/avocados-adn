/**
 * 
 */
package adn.model.entities.metadata;

/**
 * @author Ngoc Huy
 *
 */
public class _FullyAuditedEntity extends _PermanentEntity
		implements _NamedResource, _ApprovableResource, _AuditableResource {

	public static final int MINIMUM_NAME_LENGTH = 1;
	public static final int MAXIMUM_NAME_LENGTH = 255;

}
