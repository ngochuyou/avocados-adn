/**
 * 
 */
package adn.model.entities;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Entity
public class Customer extends Account {

	protected String address;

	@Column(name = "prestige_point")
	protected float prestigePoint;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public float getPrestigePoint() {
		return prestigePoint;
	}

	public void setPrestigePoint(float prestigePoint) {
		this.prestigePoint = prestigePoint;
	}

}
