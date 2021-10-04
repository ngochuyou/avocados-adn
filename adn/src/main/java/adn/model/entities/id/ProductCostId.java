/**
 * 
 */
package adn.model.entities.id;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.CreationTimestamp;

import adn.application.Common;
import adn.model.entities.metadata._ProductCost;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
@Embeddable
public class ProductCostId implements Serializable {

	@Column(name = _ProductCost.$productId, nullable = false, updatable = false)
	private BigInteger productId;

	@Column(name = _ProductCost.$providerId, nullable = false, updatable = false, columnDefinition = Common.MYSQL_UUID_COLUMN_DEFINITION)
	private UUID providerId;

	@CreationTimestamp
	@Column(name = "created_timestamp", nullable = false, updatable = false)
	private LocalDateTime createdTimestamp;

	public ProductCostId() {}

	public ProductCostId(BigInteger productId, UUID providerId, LocalDateTime createdTimestamp) {
		super();
		this.productId = productId;
		this.providerId = providerId;
		this.createdTimestamp = createdTimestamp;
	}

	public LocalDateTime getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(LocalDateTime approvedTimestamp) {
		this.createdTimestamp = approvedTimestamp;
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

		return createdTimestamp.equals(other.createdTimestamp) && productId.equals(other.productId)
				&& providerId.equals(other.providerId);
	}

	@Override
	public int hashCode() {
		int hash = 17;

		hash += 37 * productId.hashCode();
		hash += 37 * providerId.hashCode();
		hash += createdTimestamp != null ? createdTimestamp.hashCode() : 0;

		return hash;
	}

}
