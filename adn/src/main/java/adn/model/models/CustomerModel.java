/**
 * 
 */
package adn.model.models;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import adn.model.Generic;
import adn.model.entities.Customer;

/**
 * @author Ngoc Huy
 *
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Generic(entityGene = Customer.class)
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
