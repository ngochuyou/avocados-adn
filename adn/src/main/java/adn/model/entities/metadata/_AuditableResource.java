/**
 * 
 */
package adn.model.entities.metadata;

/**
 * @author Ngoc Huy
 *
 */
public interface _AuditableResource {

	String createdDate = "createdDate";
	String $createdDate = "created_date";

	String lastModifiedDate = "lastModifiedDate";
	String $lastModifiedDate = "last_modified_date";

	String createdBy = "createdBy";
	String $createdBy = "created_by";

	String lastModifiedBy = "lastModifiedBy";
	String $lastModifiedBy = "last_modified_by";

	String auditInformations = "auditInformations";
	
}
