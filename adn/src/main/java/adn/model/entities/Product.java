/**
 * 
 */
package adn.model.entities;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Table(name = "products")
public class Product extends Factor {

	@Column(scale = 3, nullable = false)
	private Double price;

	@CreationTimestamp
	@Column(name = "created_timestamp", nullable = false, updatable = false)
	private LocalDateTime createdTimestamp;

	@UpdateTimestamp
	@Column(name = "updated_timestamp", nullable = false)
	private LocalDateTime updatedTimestamp;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", referencedColumnName = "id")
	private Category category;

	@OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
	private List<ProductProviderDetail> providerDetails;

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public LocalDateTime getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	public LocalDateTime getUpdatedTimestamp() {
		return updatedTimestamp;
	}

	public void setUpdatedTimestamp(LocalDateTime updatedTimestamp) {
		this.updatedTimestamp = updatedTimestamp;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public List<ProductProviderDetail> getProviderDetails() {
		return providerDetails;
	}

	public void setProviderDetails(List<ProductProviderDetail> providerDetails) {
		this.providerDetails = providerDetails;
	}

}
