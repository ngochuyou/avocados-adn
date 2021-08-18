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

	@Column(name = "product_id")
	private String productId;

	@Column(name = "provider_id")
	private UUID providerId;

	@CreationTimestamp
	@Column(name = "applied_timestamp", nullable = false, updatable = false)
	private LocalDateTime appliedTimestamp;

	public ProductProviderDetailId() {}

	public ProductProviderDetailId(String productId, UUID providerId, LocalDateTime appliedTimestamp) {
		super();
		this.productId = productId;
		this.providerId = providerId;
		this.appliedTimestamp = appliedTimestamp;
	}

	public LocalDateTime getAppliedTimestamp() {
		return appliedTimestamp;
	}

	public void setAppliedTimestamp(LocalDateTime appliedTimestamp) {
		this.appliedTimestamp = appliedTimestamp;
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

}
