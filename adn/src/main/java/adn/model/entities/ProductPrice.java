/**
 * 
 */
package adn.model.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import adn.application.Common;
import adn.model.entities.id.ProductPriceId;
import adn.model.entities.metadata._ProductPrice;

/**
 * @author Ngoc Huy
 *
 */
@javax.persistence.Entity
@Table(name = "product_prices")
public class ProductPrice extends PermanentEntity implements ApprovableResource, SpannedResource<LocalDateTime> {

	@EmbeddedId
	private ProductPriceId id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = _ProductPrice.$productId, referencedColumnName = _ProductPrice.$id, nullable = false, columnDefinition = Common.MYSQL_BIGINT_COLUMN_DEFINITION)
	@MapsId(_ProductPrice.productId)
	private Product product;

	@Column(nullable = false, columnDefinition = Common.MYSQL_CURRENCY_COLUMN_DEFINITION)
	private BigDecimal price;

	@Embedded
	private ApprovalInformations approvalInformations;

	public ProductPriceId getId() {
		return id;
	}

	public void setId(ProductPriceId id) {
		this.id = id;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	@Override
	public ApprovalInformations getApprovalInformations() {
		if (approvalInformations == null) {
			approvalInformations = new ApprovalInformations();
		}

		return approvalInformations;
	}

	public void setApprovalInformations(ApprovalInformations approvalInformations) {
		this.approvalInformations = Optional.ofNullable(approvalInformations).orElse(new ApprovalInformations());
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	@Override
	public LocalDateTime getAppliedTimestamp() {
		return Optional.ofNullable(id).map(ProductPriceId::getAppliedTimestamp).orElse(null);
	}

	@Override
	public LocalDateTime getDroppedTimestamp() {
		return Optional.ofNullable(id).map(ProductPriceId::getDroppedTimestamp).orElse(null);
	}

}
