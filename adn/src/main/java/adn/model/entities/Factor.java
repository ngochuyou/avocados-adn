/**
 * 
 */
package adn.model.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Ngoc Huy
 *
 */
@MappedSuperclass
public abstract class Factor extends Entity {

	@Transient
	public static transient final String ACTIVE_FIELD_NAME = "active";

	@Column(nullable = false, unique = true)
	private String name;

	@Column(name = "created_by", nullable = false)
	private String createdBy;

	@Column(name = "updated_by", nullable = false)
	private String updatedBy;

	@JsonProperty
	@Column(name = "active", nullable = false)
	private Boolean active;

	@Column(name = "deactivated_date")
	private LocalDateTime deactivatedDate;

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
