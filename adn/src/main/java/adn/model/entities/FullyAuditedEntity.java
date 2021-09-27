/**
 * 
 */
package adn.model.entities;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;

/**
 * @author Ngoc Huy
 *
 */
@MappedSuperclass
public abstract class FullyAuditedEntity<ID> extends PermanentEntity
		implements AuditableResource<ID>, NamedResource, ApprovableResource {

	@Column(nullable = false, unique = true)
	private String name;

	@Embedded
	private AuditInformations auditInformations = new AuditInformations();

	@Embedded
	private ApprovalInformations approvalInformations = new ApprovalInformations();

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public AuditInformations getAuditInformations() {
		return auditInformations;
	}

	@Override
	public ApprovalInformations getApprovalInformations() {
		return approvalInformations;
	}

	public void setAuditInformations(AuditInformations auditInformations) {
		this.auditInformations = auditInformations;
	}

	public void setApprovalInformations(ApprovalInformations approvalInformations) {
		this.approvalInformations = approvalInformations;
	}

}
