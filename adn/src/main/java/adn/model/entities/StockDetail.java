/**
 * 
 */
package adn.model.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import adn.model.entities.constants.NamedSize;
import adn.model.entities.constants.Status;
import adn.model.entities.generators.StockDetailIdGenerator;
import adn.model.entities.metadata._Product;

/**
 * @author Ngoc Huy
 *
 */
@javax.persistence.Entity
@Table(name = "stock_details")
public class StockDetail extends Entity {

	public static transient final int IDENTIFIER_LENGTH = _Product.ID_LENGTH + 8 + 1; // 8 + delimiter
	public static transient final int NAMED_SIZE_MAXIMUM_LENGTH = 4;
	public static transient final int NAMED_COLOR_MAXIMUM_LENGTH = 50;
	public static transient final int MATERIAL_MAXIMUM_LENGTH = 50;
	public static transient final int STATUS_MAXIMUM_LENGTH = 50;
	public static transient final int DESCRIPTION_MAXIMUM_LENGTH = 255;
	public static transient final int NUMERIC_SIZE_MAXIMUM_VALUE = 255; // UNSIGNED
	public static transient final int NUMERIC_SIZE_MINIMUM_VALUE = 1;

	public static transient final String ID_FIELD_NAME = "id";
	public static transient final String SIZE_FIELD_NAME = "size";
	public static transient final String NUMERIC_SIZE_FIELD_NAME = "numericSize";
	public static transient final String COLOR_FIELD_NAME = "color";
	public static transient final String MATERIAL_FIELD_NAME = "material";
	public static transient final String STATUS_FIELD_NAME = "status";
	public static transient final String DESCRIPTION_FIELD_NAME = "description";
	public static transient final String ACTIVE_FIELD_NAME = "active";
	public static transient final String PRODUCT_FIELD_NAME = "product";

	@Id
	@GeneratedValue(generator = StockDetailIdGenerator.NAME)
	@GenericGenerator(name = StockDetailIdGenerator.NAME, strategy = StockDetailIdGenerator.PATH)
	@Column(columnDefinition = "VARCHAR(20)", updatable = false)
	private String id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
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

	@CreationTimestamp
	@Column(name = "stocked_timestamp", nullable = false, updatable = false)
	private LocalDateTime stockedTimestamp;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(referencedColumnName = "id", name = "stocked_by")
	private Personnel stockedBy;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(referencedColumnName = "id")
	private Provider provider;

	@Enumerated
	@Column(nullable = false, columnDefinition = "VARCHAR(20)")
	private Status status;

	@Column(nullable = false)
	private Boolean active;

	private String description;

	@Column(nullable = false, name = "updated_by")
	private String updatedBy;

	@UpdateTimestamp
	@Column(nullable = false, name = "updated_timestamp")
	private LocalDateTime updatedTimeStamp;

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

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public LocalDateTime getUpdatedTimeStamp() {
		return updatedTimeStamp;
	}

	public void setUpdatedTimeStamp(LocalDateTime updatedTimeStamp) {
		this.updatedTimeStamp = updatedTimeStamp;
	}

	public Personnel getStockedBy() {
		return stockedBy;
	}

	public void setStockedBy(Personnel stockedBy) {
		this.stockedBy = stockedBy;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@JsonProperty(value = "active")
	public Boolean isActive() {
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

	public Boolean getActive() {
		return active;
	}

	public LocalDateTime getStockedTimestamp() {
		return stockedTimestamp;
	}

	public void setStockedTimestamp(LocalDateTime stockedTimestamp) {
		this.stockedTimestamp = stockedTimestamp;
	}

}
