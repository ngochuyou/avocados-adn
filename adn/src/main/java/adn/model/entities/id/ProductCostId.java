/**
 * 
 */
package adn.model.entities.id;

import static adn.helpers.Utils.localDateTime;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import adn.application.Common;
import adn.model.entities.metadata._ProductCost;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
@Embeddable
public class ProductCostId implements Serializable {

	@Column(name = _ProductCost.$productId, nullable = false, columnDefinition = Common.MYSQL_BIGINT_COLUMN_DEFINITION)
	private BigInteger productId;

	@Column(name = _ProductCost.$providerId, nullable = false, columnDefinition = Common.MYSQL_UUID_COLUMN_DEFINITION)
	private UUID providerId;

	@Column(nullable = false)
	private LocalDateTime appliedTimestamp;

	@Column(nullable = false)
	private LocalDateTime droppedTimestamp;

	public ProductCostId() {}

	public ProductCostId(BigInteger productId, UUID providerId, LocalDateTime appliedTimeStamp,
			LocalDateTime droppedTimestamp) {
		super();
		this.productId = productId;
		this.providerId = providerId;
		this.appliedTimestamp = appliedTimeStamp;
		this.droppedTimestamp = droppedTimestamp;
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		ProductCostId other = (ProductCostId) obj;

		return appliedTimestamp.equals(other.appliedTimestamp) && productId.equals(other.productId)
				&& providerId.equals(other.providerId) && droppedTimestamp.equals(other.droppedTimestamp);
	}

	@Override
	public int hashCode() {
		int hash = 17;

		hash += 37 * productId.hashCode();
		hash += 37 * providerId.hashCode();
		hash += 37 * appliedTimestamp.hashCode();
		hash += 37 * droppedTimestamp.hashCode();

		return hash;
	}

	@Override
	public String toString() {
		return String.format("Cost for product %s from provider %s, applied on %s and dropped on %s", productId,
				providerId, localDateTime(appliedTimestamp), localDateTime(droppedTimestamp));
	}

}
