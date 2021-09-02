/**
 * 
 */
package adn.model.entities;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Ngoc Huy
 *
 */
@MappedSuperclass
public abstract class PermanentEntity extends Entity {

	@Column(nullable = false)
	private Boolean active;

	@JsonProperty("active")
	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

}
