/**
 * 
 */
package adn.model.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import adn.model.entities.converters.StringListConverter;
import adn.model.entities.generators.ProductCodeGenerator;
import adn.model.entities.metadata._Category;
import adn.model.entities.metadata._Item;
import adn.model.entities.metadata._Product;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Table(name = "products")
public class Product extends FullyAuditedEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@GeneratedValue(generator = ProductCodeGenerator.NAME)
	@GenericGenerator(name = ProductCodeGenerator.NAME, strategy = ProductCodeGenerator.PATH)
	@Column(updatable = false, length = _Product.CODE_LENGTH, unique = true)
	private String code;

	@Column(columnDefinition = "VARCHAR(50)")
	private String material;
	// IDENTIFIER_LENGTH
	@Column(columnDefinition = "VARCHAR(500)")
	@Convert(converter = StringListConverter.class)
	private List<String> images;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column
	private Float rating;

	@Column(nullable = false)
	private Boolean locked;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", referencedColumnName = _Category.id)
	private Category category;

	@JsonIgnore
	@OneToMany(mappedBy = _Item.product, fetch = FetchType.LAZY)
	private List<Item> items;

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public List<String> getImages() {
		return images;
	}

	public void setImages(List<String> images) {
		this.images = images;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Float getRating() {
		return rating;
	}

	public void setRating(Float rating) {
		this.rating = rating;
	}

	public List<Item> getStockDetails() {
		return items;
	}

	public void setStockDetails(List<Item> stockDetails) {
		this.items = stockDetails;
	}

	public String getMaterial() {
		return material;
	}

	public void setMaterial(String material) {
		this.material = material;
	}

	@JsonProperty("locked")
	public Boolean isLocked() {
		return locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

}
