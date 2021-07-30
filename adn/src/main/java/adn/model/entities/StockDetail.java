/**
 * 
 */
package adn.model.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import adn.model.entities.constants.NamedSize;
import adn.model.entities.constants.Status;
import adn.model.entities.generators.StockDetailIdGenerator;

/**
 * @author Ngoc Huy
 *
 */
@javax.persistence.Entity
@Table(name = "stock_details")
public class StockDetail extends Entity {

	public static final int IDENTIFIER_LENGTH = Product.IDENTIFIER_LENGTH + 8 + 1; // 8 + delimiter
	public static final int NAMED_SIZE_MAXIMUM_LENGTH = 4;
	public static final int NAMED_COLOR_MAXIMUM_LENGTH = 50;
	public static final int MATERIAL_MAXIMUM_LENGTH = 50;
	public static final int STATUS_MAXIMUM_LENGTH = 50;
	public static final int DESCRIPTION_MAXIMUM_LENGTH = 50;
	public static final int NUMERIC_SIZE_MAXIMUM_VALUE = 255; // UNSIGNED

	@Id
	@GeneratedValue(generator = StockDetailIdGenerator.NAME)
	@GenericGenerator(name = StockDetailIdGenerator.NAME, strategy = StockDetailIdGenerator.PATH)
	@Column(columnDefinition = "VARCHAR(20)", updatable = false)
	private String id;

	@ManyToOne(optional = false)
	private Product product;

	@Enumerated
	@Column(columnDefinition = "VARCHAR(4)")
	private NamedSize size;

	@Column(name = "numeric_size", columnDefinition = "TINYINT UNSIGNED")
	private Integer numericSize;

	@Column(name = "color", columnDefinition = "VARCHAR(50)", nullable = false)
	private String color;

	@Column(columnDefinition = "VARCHAR(50)")
	private String material;

	@Column(name = "stocked_date", nullable = false)
	private LocalDate stockedDate;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(referencedColumnName = "id", name = "stocked_by")
	private Personnel stockedBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "id", name = "sold_by")
	private Personnel soldBy;

	@ManyToOne(optional = false)
	@JoinColumn(referencedColumnName = "id")
	private Provider provider;

	@Enumerated
	@Column(nullable = false, columnDefinition = "VARCHAR(20)")
	private Status status;

	@Column(nullable = false)
	private Boolean active;

	private String description;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public NamedSize getSize() {
		return size;
	}

	public void setSize(NamedSize size) {
		this.size = size;
	}

	public Integer getNumericSize() {
		return numericSize;
	}

	public void setNumericSize(Integer numericSize) {
		this.numericSize = numericSize;
	}

	public LocalDate getStockedDate() {
		return stockedDate;
	}

	public void setStockedDate(LocalDate stockedDate) {
		this.stockedDate = stockedDate;
	}

	public Personnel getStockedBy() {
		return stockedBy;
	}

	public void setStockedBy(Personnel stockedBy) {
		this.stockedBy = stockedBy;
	}

	public Boolean getActive() {
		return active;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public Personnel getSoldBy() {
		return soldBy;
	}

	public void setSoldBy(Personnel soldBy) {
		this.soldBy = soldBy;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMaterial() {
		return material;
	}

	public void setMaterial(String material) {
		this.material = material;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

}
