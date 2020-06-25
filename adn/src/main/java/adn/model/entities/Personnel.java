/**
 * 
 */
package adn.model.entities;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Entity
public class Personnel extends Account {

	@Column(name = "created_by")
	protected String createdBy;

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

}
