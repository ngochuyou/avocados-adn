/**
 * 
 */
package adn.model.entities;

import static adn.application.Common.SHARED_TABLE_GENERATOR;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import adn.application.Common;
import adn.model.entities.constants.ItemStatus;
import adn.model.entities.constants.NamedSize;
import adn.model.entities.metadata._Customer;
import adn.model.entities.metadata._Item;
import adn.model.entities.metadata._Order;
import adn.model.entities.metadata._Product;
import adn.model.entities.metadata._Provider;

/**
 * @author Ngoc Huy
 *
 */
@javax.persistence.Entity
@Table(name = "items", indexes = @Index(columnList = _Item.indexName))
public class Item extends PermanentEntity implements AuditableResource<BigInteger>, Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = SHARED_TABLE_GENERATOR)
	@TableGenerator(name = SHARED_TABLE_GENERATOR, initialValue = Common.CROCKFORD_10A
			- 1, table = Common.SHARED_TABLE_GENERATOR_TABLENAME, allocationSize = 100)
	@Column(updatable = false, columnDefinition = Common.MYSQL_BIGINT_COLUMN_DEFINITION)
	private BigInteger id;

	@Column(unique = true)
	private String code;

	@Enumerated(EnumType.STRING)
	@Column(length = _Item.MAXIMUM_NAMED_SIZE_LENGTH)
	private NamedSize namedSize;

	@Column(columnDefinition = "TINYINT UNSIGNED")
	private Integer numericSize;

	@Column(length = _Item.MAXIMUM_NAMED_COLOR_LENGTH, nullable = false)
	private String color;

	private String note;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "VARCHAR(20)")
	private ItemStatus status;

	@Column(nullable = false, columnDefinition = Common.MYSQL_CURRENCY_COLUMN_DEFINITION)
	private BigDecimal cost;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(referencedColumnName = _Provider.$id)
	private Provider provider;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(referencedColumnName = _Product.$id, columnDefinition = Common.MYSQL_BIGINT_COLUMN_DEFINITION)
	private Product product;

	@Embedded
	private AuditInformations auditInformations;
	// @formatter:off
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(
			name = _Item.jnOrderDetails,
			joinColumns = @JoinColumn(name = _Item.jnOrderDetailsId, referencedColumnName = _Item.$id),
			inverseJoinColumns = @JoinColumn(name = _Order.jnOrderDetailsId, referencedColumnName = _Order.$id))
	private List<Order> orders;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = _Item.jnCart,
			joinColumns = @JoinColumn(name = _Item.jnCartId, referencedColumnName = _Item.$id),
			inverseJoinColumns = @JoinColumn(name = _Customer.jnCartId, referencedColumnName = _Customer.$id))
	private List<Customer> cart;
	// @formatter:on

	public Item(BigInteger id) {
		super();
		this.id = id;
	}

	public Item() {
		super();
	}

	@Override
	public BigInteger getId() {
		return id;
	}

	public void setId(BigInteger id) {
		this.id = id;
	}

	public NamedSize getNamedSize() {
		return namedSize;
	}

	public void setNamedSize(NamedSize size) {
		this.namedSize = size;
	}

	public Integer getNumericSize() {
		return numericSize;
	}

	public void setNumericSize(Integer numericSize) {
		this.numericSize = numericSize;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public ItemStatus getStatus() {
		return status;
	}

	public void setStatus(ItemStatus status) {
		this.status = status;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String description) {
		this.note = description;
	}

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public AuditInformations getAuditInformations() {
		if (auditInformations == null) {
			auditInformations = new AuditInformations();
		}

		return auditInformations;
	}

	public void setAuditInformations(AuditInformations auditInformations) {
		this.auditInformations = Optional.ofNullable(auditInformations).orElse(new AuditInformations());
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public List<Customer> getCart() {
		return cart;
	}

	public void setCart(List<Customer> cart) {
		this.cart = cart;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (getClass() != obj.getClass())
			return false;
		
		Item other = (Item) obj;
		
		return Objects.equals(id, other.id);
	}

}
