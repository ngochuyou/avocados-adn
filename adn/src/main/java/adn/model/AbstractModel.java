/**
 * 
 */
package adn.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Ngoc Huy
 *
 */
@MappedSuperclass
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractModel {

	@CreationTimestamp
	@Column(name = "created_date", nullable = false)
	protected Date createdDate;

	@UpdateTimestamp
	@Column(name = "updated_date", nullable = false)
	protected Date updatedDate;

	@JsonProperty
	@Column(name = "is_active", nullable = false)
	protected boolean isActive;

	@Column(name = "deactivated_date")
	protected Date deactivatedDate;

	public abstract Serializable getId();

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public Date getDeactivatedDate() {
		return deactivatedDate;
	}

	public void setDeactivatedDate(Date deactivatedDate) {
		this.deactivatedDate = deactivatedDate;
	}

}
