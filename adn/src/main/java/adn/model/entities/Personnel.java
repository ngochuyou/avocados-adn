/**
 * 
 */
package adn.model.entities;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Table(name = "personnels")
public class Personnel extends Operator implements AuditableResource<String> {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private Department department;

	@Embedded
	private AuditInformations auditInformations;

	public Personnel() {}

	public Personnel(String id) {
		setId(id);
		setRole(Role.PERSONNEL);
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	@Override
	public AuditInformations getAuditInformations() {
		return auditInformations;
	}

	public void setAuditInformations(AuditInformations auditInformations) {
		this.auditInformations = auditInformations;
	}

}
