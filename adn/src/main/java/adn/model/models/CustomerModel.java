/**
 * 
 */
package adn.model.models;

import adn.model.Modelized;
import adn.model.entities.Customer;

/**
 * @author Ngoc Huy
 *
 */
@Modelized(relation = Customer.class)
public class CustomerModel extends AccountModel {

	protected String address;

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
