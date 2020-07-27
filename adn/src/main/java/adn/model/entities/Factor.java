/**
 * 
 */
package adn.model.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

import adn.model.Entity;

/**
 * @author Ngoc Huy
 *
 */
@MappedSuperclass
public class Factor extends Entity {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	protected String id;

	@Column(nullable = false)
	protected String name;

	@Column(name = "created_by")
	protected String createdBy;

	@Column(name = "updated_by")
	protected String updatedBy;

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	@Override
	public void setId(Serializable id) {
		// TODO Auto-generated method stub
		this.id = (String) id;
	}

}
