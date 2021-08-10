/**
 * 
 */
package adn.model.entities;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonProperty;

import adn.model.DepartmentScoped;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Table(name = "departments")
public class Department extends adn.model.entities.Entity implements DepartmentScoped {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(columnDefinition = "BINARY(16)")
	protected UUID id;

	@Column(nullable = false, unique = true)
	protected String name;

	@Column(nullable = false)
	protected Boolean active;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("active")
	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

}
