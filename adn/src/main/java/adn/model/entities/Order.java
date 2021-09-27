/**
 * 
 */
package adn.model.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import adn.application.Common;
import adn.model.entities.constants.OrderStatus;
import adn.model.entities.generators.OrderCodeGenerator;
import adn.model.entities.metadata._Customer;
import adn.model.entities.metadata._Item;
import adn.model.entities.metadata._Operator;
import adn.model.entities.metadata._Order;
import adn.model.entities.metadata._Product;
import adn.model.entities.metadata._User;

/**
 * @author Ngoc Huy
 *
 */
@javax.persistence.Entity
@Table(name = "orders")
public class Order extends PermanentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@GeneratedValue(generator = OrderCodeGenerator.NAME)
	@GenericGenerator(name = OrderCodeGenerator.NAME, strategy = OrderCodeGenerator.PATH)
	@Column(nullable = false, updatable = false, length = _Product.CODE_LENGTH, unique = true)
	private String code;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status;

	@Column(name = "delivery_fee", columnDefinition = Common.CURRENCY_MYSQL_COLUMN_DEFINITION)
	private BigDecimal deliveryFee;

	@CreationTimestamp
	@Column(name = "created_timestamp", nullable = false, updatable = false)
	private LocalDateTime createdTimestamp;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = _Customer.$id, nullable = false, updatable = false)
	private Customer customer;

	@UpdateTimestamp
	@Column(name = "updated_timestamp", nullable = false)
	private LocalDateTime updatedTimestamp;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by", referencedColumnName = _User.$id, nullable = false)
	private User updatedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "handled_by", referencedColumnName = _Operator.$id, updatable = false)
	private Operator handledBy;
	// @formatter:off
	@ManyToMany
	@JoinTable(
			name = "order_details",
			joinColumns = @JoinColumn(name = "order_id", referencedColumnName = _Order.$id),
			inverseJoinColumns = @JoinColumn(name = "item_id", referencedColumnName = _Item.$id))
	private List<Item> items;
	// @formatter:on
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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

	public User getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(User updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Operator getHandledBy() {
		return handledBy;
	}

	public void setHandledBy(Operator handledBy) {
		this.handledBy = handledBy;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

}
