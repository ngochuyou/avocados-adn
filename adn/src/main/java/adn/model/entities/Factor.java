/**
 * 
 */
package adn.model.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Ngoc Huy
 *
 */
@MappedSuperclass
public abstract class Factor extends Entity {

	@Column(nullable = false, unique = true)
	protected String name;

	@Column(name = "created_by", nullable = false)
	protected String createdBy;

	@Column(name = "updated_by", nullable = false)
	protected String updatedBy;

	@JsonProperty
	@Column(name = "active", nullable = false)
	protected Boolean active;

	@Column(name = "deactivated_date")
	protected LocalDateTime deactivatedDate;

	@JsonProperty(value = "active")
	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean isActive) {
		this.active = isActive;
	}

	@JsonIgnore
	public LocalDateTime getDeactivatedDate() {
		return deactivatedDate;
	}

	public void setDeactivatedDate(LocalDateTime deactivatedDate) {
		this.deactivatedDate = deactivatedDate;
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

}
