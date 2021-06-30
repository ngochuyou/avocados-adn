/**
 * 
 */
package adn.model.entities;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Table(name = "product_provider_details")
public class ProductProviderDetail extends adn.model.entities.Entity {

	@EmbeddedId
	private ProductProviderDetailId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("product_id")
	private Product product;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("provider_id")
	private Provider provider;

	@Column(nullable = false, scale = 3)
	private Double price;

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

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

}

@SuppressWarnings("serial")
@Embeddable
class ProductProviderDetailId implements Serializable {

	@Column(name = "product_id")
	private UUID productId;

	@Column(name = "provider_id")
	private UUID providerId;

	public ProductProviderDetailId() {}

	public ProductProviderDetailId(UUID productId, UUID providerId) {
		super();
		this.productId = productId;
		this.providerId = providerId;
	}

	public UUID getProductId() {
		return productId;
	}

	public void setProductId(UUID productId) {
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

		if (!(obj instanceof ProductProviderDetailId))
			return false;

		ProductProviderDetailId that = (ProductProviderDetailId) obj;

		return Objects.equals(this.productId, that.productId) && Objects.equals(this.providerId, that.providerId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.productId, this.providerId);
	}

}