/**
 * 
 */
package adn.model.entities;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import adn.model.entities.converters.StringListConverter;
import adn.model.entities.generators.ProductIdGenerator;
import adn.model.entities.metadata._Product;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Table(name = "products")
public class Product extends Factor {

	@Id
	@GeneratedValue(generator = ProductIdGenerator.NAME)
	@GenericGenerator(name = ProductIdGenerator.NAME, strategy = ProductIdGenerator.PATH)
	@Column(updatable = false, length = _Product.ID_LENGTH)
	private String id;

	@Column(columnDefinition = "DECIMAL(13,4)", nullable = false)
	private BigDecimal price;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", referencedColumnName = "id")
	private Category category;
	// IDENTIFIER_LENGTH
	@Column(columnDefinition = "VARCHAR(500)")
	@Convert(converter = StringListConverter.class)
	private List<String> images;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column
	private Float rating;

	@JsonIgnore
	@OneToMany(mappedBy = "product")
	private List<StockDetail> stockDetails;

	public String getId() {
		return id;
	}

	public void setId(String code) {
		this.id = code;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
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

	public List<StockDetail> getStockDetails() {
		return stockDetails;
	}

	public void setStockDetails(List<StockDetail> stockDetails) {
		this.stockDetails = stockDetails;
	}

}
