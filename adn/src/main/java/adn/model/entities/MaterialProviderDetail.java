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
@Table(name = "material_provider_details")
public class MaterialProviderDetail extends adn.model.entities.Entity {

	@EmbeddedId
	private MaterialProviderDetailID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("material_id")
	private Material material;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("provider_id")
	private Provider provider;

	@Column(nullable = false, scale = 3)
	private Double price;

	public MaterialProviderDetailID getId() {
		return id;
	}

	public void setId(MaterialProviderDetailID id) {
		this.id = id;
	}

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

@SuppressWarnings("serial")
@Embeddable
class MaterialProviderDetailID implements Serializable {

	@Column(name = "material_id")
	private UUID materialId;

	@Column(name = "provider_id")
	private UUID providerId;

	public MaterialProviderDetailID() {}

	public MaterialProviderDetailID(UUID materialId, UUID providerId) {
		super();
		this.materialId = materialId;
		this.providerId = providerId;
	}

	public UUID getMaterialId() {
		return materialId;
	}

	public void setMaterialId(UUID materialId) {
		this.materialId = materialId;
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

		if (!(obj instanceof MaterialProviderDetailID))
			return false;

		MaterialProviderDetailID that = (MaterialProviderDetailID) obj;

		return Objects.equals(this.materialId, that.materialId) && Objects.equals(this.providerId, that.providerId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.materialId, this.providerId);
	}

}