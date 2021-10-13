/**
 * 
 */
package adn.model.entities;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Entity
public class Customer extends User {

	@Column(name = "prestige_point")
	private Float prestigePoint;

	private Boolean subscribed;

	public Customer() {
		setRole(Role.CUSTOMER);
	}
	
	public Customer(String id) {
		this();
		setId(id);
	}

	public Float getPrestigePoint() {
		return prestigePoint;
	}

	public void setPrestigePoint(Float prestigePoint) {
		this.prestigePoint = prestigePoint;
	}

	@JsonProperty("subscribed")
	public Boolean isSubscribed() {
		return subscribed;
	}

	public void setSubscribed(Boolean subscribed) {
		this.subscribed = subscribed;
	}

}
