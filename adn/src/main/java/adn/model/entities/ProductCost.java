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
import adn.model.entities.id.ProductCostId;
import adn.model.entities.metadata._Product;
import adn.model.entities.metadata._ProductCost;
import adn.model.entities.metadata._Provider;

/**
 * @author Ngoc Huy
 *
 */
@javax.persistence.Entity
@Table(name = "product_costs")
public class ProductCost extends PermanentEntity implements ApprovableResource, SpannedResource<LocalDateTime> {

	@EmbeddedId
	private ProductCostId id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = _ProductCost.$productId, referencedColumnName = _Product.$id, columnDefinition = Common.MYSQL_BIGINT_COLUMN_DEFINITION, nullable = false)
	@MapsId(_ProductCost.productId)
	private Product product;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = _ProductCost.$providerId, referencedColumnName = _Provider.$id, columnDefinition = Common.MYSQL_UUID_COLUMN_DEFINITION, updatable = false)
	@MapsId(_ProductCost.providerId)
	private Provider provider;

	@Column(columnDefinition = Common.MYSQL_CURRENCY_COLUMN_DEFINITION, nullable = false, updatable = false)
	private BigDecimal cost;

	@Embedded
	private ApprovalInformations approvalInformations;

	public ProductCostId getId() {
		return id;
	}

	public void setId(ProductCostId id) {
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

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
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
	
	@Override
	public LocalDateTime getAppliedTimestamp() {
		return Optional.ofNullable(id).map(ProductCostId::getAppliedTimestamp).orElse(null);
	}
	
	@Override
	public LocalDateTime getDroppedTimestamp() {
		return Optional.ofNullable(id).map(ProductCostId::getDroppedTimestamp).orElse(null);
	}

}
