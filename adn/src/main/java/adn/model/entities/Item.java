/**
 * 
 */
package adn.model.entities;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import adn.application.Common;
import adn.model.entities.constants.NamedSize;
import adn.model.entities.constants.Status;
import adn.model.entities.generators.ItemCodeGenerator;
import adn.model.entities.metadata._ProductPrice;
import adn.model.entities.metadata._Provider;

/**
 * @author Ngoc Huy
 *
 */
@javax.persistence.Entity
@Table(name = "items")
public class Item extends PermanentEntity implements AuditableResource<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@GeneratedValue(generator = ItemCodeGenerator.NAME)
	@GenericGenerator(name = ItemCodeGenerator.NAME, strategy = ItemCodeGenerator.PATH)
	@Column(updatable = false)
	private String code;

	@Enumerated
	@Column(name = "named_size", columnDefinition = "VARCHAR(4)")
	private NamedSize namedSize;

	@Column(name = "numeric_size", columnDefinition = "TINYINT UNSIGNED")
	private Integer numericSize;

	@Column(name = "color", columnDefinition = "VARCHAR(50)", nullable = false)
	private String color;

	private String note;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, columnDefinition = "VARCHAR(20)")
	private Status status;

	@Column(nullable = false, columnDefinition = Common.CURRENCY_MYSQL_COLUMN_DEFINITION)
	private BigDecimal cost;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(referencedColumnName = _Provider.$id)
	private Provider provider;

	@Embedded
	private AuditInformations auditInformations;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(referencedColumnName = _ProductPrice.id)
	private ProductPrice price;

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
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
		return auditInformations;
	}

	public void setAuditInformations(AuditInformations auditInformations) {
		this.auditInformations = auditInformations;
	}

	public ProductPrice getPrice() {
		return price;
	}

	public void setPrice(ProductPrice price) {
		this.price = price;
	}

}
