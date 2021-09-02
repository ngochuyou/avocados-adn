/**
 * 
 */
package adn.model.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Entity
public class Personnel extends Operator {

	@Column(name = "created_by")
	private String createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	private Department department;

	public Personnel() {}

	public Personnel(String id) {
		setId(id);
		setRole(Role.PERSONNEL);
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

}
