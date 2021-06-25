/**
 * 
 */
package adn.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Ngoc Huy
 *
 */
@MappedSuperclass
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractModel {

	@JsonProperty
	@Column(name = "is_active", nullable = false)
	protected boolean isActive;

	@Column(name = "deactivated_date")
	protected LocalDateTime deactivatedDate;

	@JsonProperty(value = "isActive")
	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public LocalDateTime getDeactivatedDate() {
		return deactivatedDate;
	}

	public void setDeactivatedDate(LocalDateTime deactivatedDate) {
		this.deactivatedDate = deactivatedDate;
	}

}
