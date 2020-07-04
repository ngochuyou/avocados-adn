/**
 * 
 */
package adn.model.entities;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

import adn.model.Model;

/**
 * @author Ngoc Huy
 *
 */
@MappedSuperclass
public class Factor extends Model {

	@Id
	@GeneratedValue
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	protected String id;

	@Column(nullable = false)
	protected String name;

	@Column(nullable = false)
	protected String createdBy;

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

}
