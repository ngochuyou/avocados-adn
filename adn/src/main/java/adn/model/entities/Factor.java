/**
 * 
 */
package adn.model.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Ngoc Huy
 *
 */
@MappedSuperclass
public abstract class Factor extends Entity {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(columnDefinition = "BINARY(16)")
	protected UUID id;

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

	@JsonProperty(value = "isActive")
	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean isActive) {
		this.active = isActive;
	}

	public LocalDateTime getDeactivatedDate() {
		return deactivatedDate;
	}

	public void setDeactivatedDate(LocalDateTime deactivatedDate) {
		this.deactivatedDate = deactivatedDate;
	}

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
