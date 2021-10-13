/**
 * 
 */
package adn.model.entities;

import static adn.application.Common.SHARED_TABLE_GENERATOR;

import java.math.BigDecimal;
import java.math.BigInteger;
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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import adn.application.Common;
import adn.model.entities.constants.NamedSize;
import adn.model.entities.constants.ItemStatus;
import adn.model.entities.metadata._Item;
import adn.model.entities.metadata._Product;
import adn.model.entities.metadata._Provider;

/**
 * @author Ngoc Huy
 *
 */
@javax.persistence.Entity
@Table(name = "items", indexes = @Index(columnList = _Item.$product))
public class Item extends PermanentEntity implements AuditableResource<BigInteger> {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = SHARED_TABLE_GENERATOR)
	@TableGenerator(name = SHARED_TABLE_GENERATOR, initialValue = Common.CROCKFORD_10A
			- 1, table = Common.SHARED_TABLE_GENERATOR_TABLENAME)
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

	@Column(nullable = false, columnDefinition = Common.MYSQL_CURRENCY_COLUMN_DEFINITION)
	private BigDecimal price;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(referencedColumnName = _Product.$id, columnDefinition = Common.MYSQL_BIGINT_COLUMN_DEFINITION)
	private Product product;

	@Embedded
	private AuditInformations auditInformations;

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

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

}
