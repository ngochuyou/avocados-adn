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
@Table(name = "material_provider_details")
@IdClass(MaterialProviderDetailID.class)
public class MaterialProviderDetail extends adn.model.entities.Entity {

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "material_id", referencedColumnName = "id")
	private Material material;

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "provider_id", referencedColumnName = "id")
	private Provider provider;

	@Column(nullable = false, scale = 3)
	private Double price;

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.material = material;
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
//class MaterialProviderDetailID implements Serializable {
//
//	@Column(name = "material_id")
//	private UUID materialId;
//
//	@Column(name = "provider_id")
//	private UUID providerId;
//
//	public MaterialProviderDetailID() {}
//
//	public MaterialProviderDetailID(UUID materialId, UUID providerId) {
//		super();
//		this.materialId = materialId;
//		this.providerId = providerId;
//	}
//
//	public UUID getMaterialId() {
//		return materialId;
//	}
//
//	public void setMaterialId(UUID materialId) {
//		this.materialId = materialId;
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
//		if (!(obj instanceof MaterialProviderDetailID))
//			return false;
//
//		MaterialProviderDetailID that = (MaterialProviderDetailID) obj;
//
//		return Objects.equals(this.materialId, that.materialId) && Objects.equals(this.providerId, that.providerId);
//	}
//
//	@Override
//	public int hashCode() {
//		return Objects.hash(this.materialId, this.providerId);
//	}
//
//}

@SuppressWarnings("serial")
class MaterialProviderDetailID implements Serializable {

	private UUID material;

	private UUID provider;

	public MaterialProviderDetailID() {}

	public MaterialProviderDetailID(UUID materialId, UUID providerId) {
		super();
		this.material = materialId;
		this.provider = providerId;
	}

	public UUID getMaterial() {
		return material;
	}

	public void setMaterial(UUID material) {
		this.material = material;
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

		if (!(obj instanceof MaterialProviderDetailID))
			return false;

		MaterialProviderDetailID that = (MaterialProviderDetailID) obj;

		return Objects.equals(this.material, that.material) && Objects.equals(this.provider, that.provider);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.material, this.provider);
	}

}