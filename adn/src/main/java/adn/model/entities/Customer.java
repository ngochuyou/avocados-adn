/**
 * 
 */
package adn.model.entities;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import adn.model.entities.metadata._Customer;
import adn.model.entities.metadata._Item;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Table(name = "customers")
public class Customer extends User {

	@Column
	private Float prestigePoint;

	private Boolean subscribed;
	// @formatter:off
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = _Customer.jnCart,
			joinColumns = @JoinColumn(name = _Customer.jnCartId, referencedColumnName = _Customer.$id),
			inverseJoinColumns = @JoinColumn(name = _Item.jnCartId, referencedColumnName = _Item.$id))
	private Set<Item> cart;
	// @formatter:on
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

	@JsonProperty(_Customer.subscribed)
	public Boolean isSubscribed() {
		return subscribed;
	}

	public void setSubscribed(Boolean subscribed) {
		this.subscribed = subscribed;
	}

	public Set<Item> getCart() {
		return cart;
	}

	public void setCart(Set<Item> cart) {
		this.cart = cart;
	}

	public Boolean getSubscribed() {
		return subscribed;
	}

}
