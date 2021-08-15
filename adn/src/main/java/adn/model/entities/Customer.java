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

	@Column(name = "prestige_point")
	private float prestigePoint;

	public float getPrestigePoint() {
		return prestigePoint;
	}

	public void setPrestigePoint(float prestigePoint) {
		this.prestigePoint = prestigePoint;
	}

}
