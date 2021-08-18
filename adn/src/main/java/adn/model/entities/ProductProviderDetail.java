/**
 * 
 */
package adn.model.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import adn.model.DepartmentScoped;
import adn.model.entities.id.ProductProviderDetailId;

/**
 * @author Ngoc Huy
 *
 */
@javax.persistence.Entity
@Table(name = "product_provider_details")
public class ProductProviderDetail extends Entity implements DepartmentScoped {

	public static transient final String ID_FIELD = "id";
	public static transient final String ID_PROVIDER_FIELD = "providerId";
	public static transient final String ID_PRODUCT_FIELD = "productId";
	public static transient final String ID_APPLIED_TIMESTAMP_FIELD = "appliedTimestamp";
	public static transient final String PRICE_FIELD = "price";
	public static transient final String DROPPED_TIMESTAMP_FIELD = "droppedTimestamp";

	@EmbeddedId
	private ProductProviderDetailId id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "product_id", updatable = false, columnDefinition = Product.ID_COLUMN_DEFINITION)
	@MapsId("productId")
	@JsonIgnore
	private Product product;

	@ManyToOne(optional = false)
	@JoinColumn(name = "provider_id", updatable = false, columnDefinition = "BINARY(16)")
	@MapsId("providerId")
	@JsonIgnore
	private Provider provider;

	@Column(name = "dropped_timestamp")
	private LocalDateTime droppedTimestamp;

	@Column(columnDefinition = "DECIMAL(13,4)", nullable = false)
	private BigDecimal price;

	@Column(nullable = false, name = "created_by")
	private String createdBy;

	@Column(name = "dropped_by")
	private String droppedBy;

	public ProductProviderDetailId getId() {
		return id;
	}

	public void setId(ProductProviderDetailId id) {
		this.id = id;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public LocalDateTime getDroppedTimestamp() {
		return droppedTimestamp;
	}

	public void setDroppedTimestamp(LocalDateTime droppedTimestamp) {
		this.droppedTimestamp = droppedTimestamp;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getDroppedBy() {
		return droppedBy;
	}

	public void setDroppedBy(String droppedBy) {
		this.droppedBy = droppedBy;
	}

}
