/**
 * 
 */
package adn.model.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import adn.model.DepartmentScoped;
import adn.model.entities.id.ProductProviderDetailId;
import adn.model.entities.metadata._Account;
import adn.model.entities.metadata._Product;
import adn.model.entities.metadata._ProductProviderDetail;

/**
 * @author Ngoc Huy
 *
 */
@javax.persistence.Entity
@Table(name = "product_provider_details")
public class ProductProviderDetail extends PermanentEntity implements DepartmentScoped {

	@EmbeddedId
	private ProductProviderDetailId id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false, columnDefinition = _Product.ID_COLUMN_DEFINITION)
	@MapsId(_ProductProviderDetail.productId)
	private Product product;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "provider_id", nullable = false, columnDefinition = "BINARY(16)")
	@MapsId(_ProductProviderDetail.providerId)
	private Provider provider;

	@Column(name = "dropped_timestamp")
	private LocalDateTime droppedTimestamp;

	@Column(columnDefinition = "DECIMAL(13,4)", nullable = false)
	private BigDecimal price;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by", referencedColumnName = _Account.id)
	private Operator createdBy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "approved_by", referencedColumnName = _Account.id)
	private Head approvedBy;

	@Column(name = "approved_timestamp")
	private LocalDateTime approvedTimestamp;

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

	public LocalDateTime getApprovedTimestamp() {
		return approvedTimestamp;
	}

	public void setApprovedTimestamp(LocalDateTime approvedTimestamp) {
		this.approvedTimestamp = approvedTimestamp;
	}

	public Operator getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Operator createdBy) {
		this.createdBy = createdBy;
	}

	public Head getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(Head approvedBy) {
		this.approvedBy = approvedBy;
	}

}
