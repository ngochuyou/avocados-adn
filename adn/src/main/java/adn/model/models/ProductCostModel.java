/**
 * 
 */
package adn.model.models;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;

import adn.application.Common;
import adn.model.Generic;
import adn.model.entities.ProductCost;
import adn.model.factory.extraction.MappedTo;
import adn.model.factory.extraction.Synthesized;

/**
 * @author Ngoc Huy
 *
 */
@Generic(entityGene = ProductCost.class)
@Synthesized
public class ProductCostModel extends Model {

	@MappedTo({ "id.productId", "product.id" })
	private BigInteger productId;

	@MappedTo({ "id.providerId", "provider.id" })
	private UUID providerId;

	@MappedTo("id.appliedTimestamp")
	@JsonFormat(pattern = Common.COMMON_LDT_FORMAT)
	private LocalDateTime appliedTimestamp;

	@MappedTo("id.droppedTimestamp")
	@JsonFormat(pattern = Common.COMMON_LDT_FORMAT)
	private LocalDateTime droppedTimestamp;

	private BigDecimal cost;

	public BigInteger getProductId() {
		return productId;
	}

	public void setProductId(BigInteger productId) {
		this.productId = productId;
	}

	public UUID getProviderId() {
		return providerId;
	}

	public void setProviderId(UUID providerId) {
		this.providerId = providerId;
	}

	public LocalDateTime getAppliedTimestamp() {
		return appliedTimestamp;
	}

	public void setAppliedTimestamp(LocalDateTime appliedTimestamp) {
		this.appliedTimestamp = appliedTimestamp;
	}

	public LocalDateTime getDroppedTimestamp() {
		return droppedTimestamp;
	}

	public void setDroppedTimestamp(LocalDateTime droppedTimestamp) {
		this.droppedTimestamp = droppedTimestamp;
	}

	public BigDecimal getCost() {
		return cost;
	}

	public void setCost(BigDecimal cost) {
		this.cost = cost;
	}

}
