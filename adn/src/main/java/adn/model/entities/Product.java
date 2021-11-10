/**
 * 
 */
package adn.model.entities;

import static adn.application.Common.SHARED_TABLE_GENERATOR;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.fasterxml.jackson.annotation.JsonProperty;

import adn.application.Common;
import adn.model.entities.converters.StringListConverter;
import adn.model.entities.metadata._Category;
import adn.model.entities.metadata._Product;
import adn.model.entities.metadata._ProductCost;
import adn.model.entities.metadata._ProductPrice;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Table(name = "products", indexes = @Index(columnList = _Product.indexName))
public class Product extends FullyAuditedEntity<BigInteger> {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = SHARED_TABLE_GENERATOR)
	@TableGenerator(name = SHARED_TABLE_GENERATOR, initialValue = Common.CROCKFORD_10A
			- 1, allocationSize = 1, table = Common.SHARED_TABLE_GENERATOR_TABLENAME)
	@Column(updatable = false, columnDefinition = Common.MYSQL_BIGINT_COLUMN_DEFINITION)
	private BigInteger id;

	@Column(unique = true)
	private String code;

	@Column(length = _Product.MAXIMUM_MATERIAL_LENGTH)
	private String material;
	// IDENTIFIER_LENGTH
	@Column(length = _Product.MAXIMUM_IMAGES_COLUMN_LENGTH)
	@Convert(converter = StringListConverter.class)
	private List<String> images;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column
	private Float rating;

	@Column(nullable = false)
	private Boolean locked;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = _Product.$category, referencedColumnName = _Category.$id)
	private Category category;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = _ProductCost.product)
	private List<ProductCost> costs;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = _ProductPrice.product)
	private List<ProductPrice> prices;

	public Product() {}

	public Product(BigInteger id) {
		this.id = id;
	}

	@Override
	public BigInteger getId() {
		return id;
	}

	public void setId(BigInteger id) {
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

	public Boolean getLocked() {
		return locked;
	}

	public List<ProductCost> getCosts() {
		return costs;
	}

	public List<ProductPrice> getPrices() {
		return prices;
	}

	public void setPrices(List<ProductPrice> prices) {
		this.prices = prices;
	}

	public void setCosts(List<ProductCost> costs) {
		this.costs = costs;
	}

}
