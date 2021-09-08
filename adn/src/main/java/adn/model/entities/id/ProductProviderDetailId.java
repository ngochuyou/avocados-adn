/**
 * 
 */
package adn.model.entities.id;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.CreationTimestamp;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
@Embeddable
public class ProductProviderDetailId implements Serializable {

	@Column(name = "product_id", nullable = false)
	private String productId;

	@Column(name = "provider_id", nullable = false)
	private UUID providerId;

	@CreationTimestamp
	@Column(name = "created_timestamp", nullable = false, updatable = false)
	private LocalDateTime createdTimestamp;

	public ProductProviderDetailId() {}

	public ProductProviderDetailId(String productId, UUID providerId, LocalDateTime createdTimestamp) {
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

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
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

		ProductProviderDetailId other = (ProductProviderDetailId) obj;

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
