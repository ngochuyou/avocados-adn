/**
 * 
 */
package adn.model.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

/**
 * @author Ngoc Huy
 *
 */
@Entity
public class Personnel extends Account {

	@Column(name = "created_by")
	protected String createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	protected Department department;

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
