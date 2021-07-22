/**
 * 
 */
package adn.model.entities;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Table(name = "product_provider_details")
@IdClass(ProductProviderDetailId.class)
public class ProductProviderDetail extends adn.model.entities.Entity {

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", referencedColumnName = "id", updatable = false, columnDefinition = "VARCHAR(11)")
	private Product product;

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "provider_id", referencedColumnName = "id")
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

//@SuppressWarnings("serial")
//@Embeddable
//class ProductProviderDetailId implements Serializable {
//
//	@Column(name = "product_id")
//	private UUID productId;
//
//	@Column(name = "provider_id")
//	private UUID providerId;
//
//	public ProductProviderDetailId() {}
//
//	public ProductProviderDetailId(UUID productId, UUID providerId) {
//		super();
//		this.productId = productId;
//		this.providerId = providerId;
//	}
//
//	public UUID getProductId() {
//		return productId;
//	}
//
//	public void setProductId(UUID productId) {
//		this.productId = productId;
//	}
//
//	public UUID getProviderId() {
//		return providerId;
//	}
//
//	public void setProviderId(UUID providerId) {
//		this.providerId = providerId;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//
//		if (!(obj instanceof ProductProviderDetailId))
//			return false;
//
//		ProductProviderDetailId that = (ProductProviderDetailId) obj;
//
//		return Objects.equals(this.productId, that.productId) && Objects.equals(this.providerId, that.providerId);
//	}
//
//	@Override
//	public int hashCode() {
//		return Objects.hash(this.productId, this.providerId);
//	}
//
//}

@SuppressWarnings("serial")
class ProductProviderDetailId implements Serializable {

	private String product;

	private UUID provider;

	public ProductProviderDetailId() {}

	public ProductProviderDetailId(String productId, UUID providerId) {
		super();
		this.product = productId;
		this.provider = providerId;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public UUID getProvider() {
		return provider;
	}

	public void setProvider(UUID provider) {
		this.provider = provider;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof ProductProviderDetailId))
			return false;

		ProductProviderDetailId that = (ProductProviderDetailId) obj;

		return Objects.equals(this.product, that.product) && Objects.equals(this.provider, that.provider);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.product, this.provider);
	}

}