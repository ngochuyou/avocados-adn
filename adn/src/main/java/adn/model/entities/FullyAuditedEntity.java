/**
 * 
 */
package adn.model.entities;

import java.util.Optional;

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
	private ApprovalInformations approvalInformations;

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public AuditInformations getAuditInformations() {
		if (auditInformations == null) {
			auditInformations = new AuditInformations();
		}

		return auditInformations;
	}

	@Override
	public ApprovalInformations getApprovalInformations() {
		if (approvalInformations == null) {
			approvalInformations = new ApprovalInformations();
		}

		return approvalInformations;
	}

	public void setAuditInformations(AuditInformations auditInformations) {
		this.auditInformations = Optional.ofNullable(auditInformations).orElse(new AuditInformations());
	}

	public void setApprovalInformations(ApprovalInformations approvalInformations) {
		this.approvalInformations = Optional.ofNullable(approvalInformations).orElse(new ApprovalInformations());
	}

}
