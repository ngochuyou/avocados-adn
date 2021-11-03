/**
 * 
 */
package adn.model.entities;

import static adn.application.Common.SHARED_TABLE_GENERATOR;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import adn.application.Common;
import adn.model.entities.constants.OrderStatus;
import adn.model.entities.metadata._Customer;
import adn.model.entities.metadata._Item;
import adn.model.entities.metadata._Order;

/**
 * @author Ngoc Huy
 *
 */
@javax.persistence.Entity
@Table(name = "orders")
public class Order extends PermanentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = SHARED_TABLE_GENERATOR)
	@TableGenerator(name = SHARED_TABLE_GENERATOR, initialValue = Common.CROCKFORD_1A
			- 1, allocationSize = 1, table = Common.SHARED_TABLE_GENERATOR_TABLENAME)
	@Column(updatable = false, columnDefinition = Common.MYSQL_BIGINT_COLUMN_DEFINITION)
	private BigInteger id;

	@Column(unique = true)
	private String code;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status;

	@Column(nullable = false)
	private String address;

	@ManyToOne(optional = false)
	private District district;

	@Column(columnDefinition = Common.MYSQL_CURRENCY_COLUMN_DEFINITION)
	private BigDecimal deliveryFee;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdTimestamp;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = _Customer.$id, nullable = false, updatable = false)
	private Customer customer;

	@UpdateTimestamp
	@Column(nullable = false)
	private LocalDateTime updatedTimestamp;

	@Column(length = _Order.MAXIMUM_NOTE_LENGTH)
	private String note;
	// @formatter:off
	@ManyToMany(cascade = CascadeType.MERGE)
	@JoinTable(
			name = _Order.jnOrderDetails,
			joinColumns = @JoinColumn(name = _Order.jnOrderDetailsId, referencedColumnName = _Order.$id),
			inverseJoinColumns = @JoinColumn(name = _Item.jnOrderDetailsId, referencedColumnName = _Item.$id))
	private Set<Item> items;
	// @formatter:on
	public BigInteger getId() {
		return id;
	}

	public void setId(BigInteger id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public BigDecimal getDeliveryFee() {
		return deliveryFee;
	}

	public void setDeliveryFee(BigDecimal deliveryFee) {
		this.deliveryFee = deliveryFee;
	}

	public LocalDateTime getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public LocalDateTime getUpdatedTimestamp() {
		return updatedTimestamp;
	}

	public void setUpdatedTimestamp(LocalDateTime updatedTimestamp) {
		this.updatedTimestamp = updatedTimestamp;
	}

	public Set<Item> getItems() {
		return items;
	}

	public void setItems(Set<Item> items) {
		this.items = items;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public District getDistrict() {
		return district;
	}

	public void setDistrict(District district) {
		this.district = district;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

}
