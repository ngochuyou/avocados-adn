/**
 * 
 */
package adn.model.entities;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import adn.application.Common;
import adn.model.entities.id.OrderDetailId;
import adn.model.entities.metadata._Item;
import adn.model.entities.metadata._Order;
import adn.model.entities.metadata._OrderDetail;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Table(name = "order_details", indexes = @Index(columnList = _OrderDetail.indexName))
public class OrderDetail extends PermanentEntity {

	@EmbeddedId
	private OrderDetailId id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = _OrderDetail.$orderId, referencedColumnName = _Order.$id, nullable = false, columnDefinition = Common.MYSQL_BIGINT_COLUMN_DEFINITION)
	@MapsId(_OrderDetail.orderId)
	private Order order;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = _OrderDetail.$itemId, referencedColumnName = _Item.$id, nullable = false, columnDefinition = Common.MYSQL_BIGINT_COLUMN_DEFINITION)
	@MapsId(_OrderDetail.itemId)
	private Item item;

	@Column
	private Integer rating;

	@Column(nullable = false, columnDefinition = Common.MYSQL_CURRENCY_COLUMN_DEFINITION)
	private BigDecimal price;

	public OrderDetail() {}

	public OrderDetail(Order order, Item item, BigDecimal price) {
		this.order = order;
		this.item = item;
		this.price = price;
	}

	public OrderDetail(BigInteger orderId, BigInteger itemId, BigDecimal price) {
		this.id = new OrderDetailId(orderId, itemId);
		this.order = new Order(orderId);
		this.item = new Item(itemId);
		this.price = price;
	}

	public OrderDetail(BigInteger orderId, BigInteger itemId, BigDecimal price, boolean active) {
		this(orderId, itemId, price);
		this.setActive(active);
	}

	public OrderDetailId getId() {
		return id;
	}

	public void setId(OrderDetailId id) {
		this.id = id;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return String.format("OrderDetail=(order=%s, item=%s, price=%s)", order.getId(), item.getId(), price);
	}

}
