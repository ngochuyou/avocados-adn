/**
 * 
 */
package adn.model.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Entity
public class Personnel extends Operator {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", referencedColumnName = "id")
	private Operator createdBy;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private Department department;

	public Personnel() {}

	public Personnel(String id) {
		setId(id);
		setRole(Role.PERSONNEL);
	}

	public Operator getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Operator createdBy) {
		this.createdBy = createdBy;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

}
