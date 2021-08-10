/**
 * 
 */
package adn.model.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import adn.model.entities.converters.StringSetConverter;
import adn.model.entities.generators.ProductIdGenerator;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Table(name = "products")
public class Product extends Factor {

	public static final int IDENTIFIER_LENGTH = Category.IDENTIFIER_LENGTH + 5 + 1; // 5 + delimiter
	public static final String ID_FIELD_NAME = "id";
	public static final String CATEGORY_FIELD_NAME = "category";
	public static final String STOCKDETAIL_FIELD_NAME = "stockDetails";

	@Id
	@GeneratedValue(generator = ProductIdGenerator.NAME)
	@GenericGenerator(name = ProductIdGenerator.NAME, strategy = ProductIdGenerator.PATH)
	@Column(updatable = false, length = IDENTIFIER_LENGTH, columnDefinition = "VARCHAR(11)")
	private String id;

	@Column(columnDefinition = "DECIMAL(13,4)", nullable = false)
	private BigDecimal price;

	@CreationTimestamp
	@Column(name = "created_timestamp", nullable = false, updatable = false)
	private LocalDateTime createdTimestamp;

	@UpdateTimestamp
	@Column(name = "updated_timestamp", nullable = false)
	private LocalDateTime updatedTimestamp;

	@ManyToOne(optional = false)
	@JoinColumn(name = "category_id", referencedColumnName = "id")
	private Category category;
	// IDENTIFIER_LENGTH
	@Column(columnDefinition = "VARCHAR(500)")
	@Convert(converter = StringSetConverter.class)
	private Set<String> images;

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

	@JsonIgnore
	public LocalDateTime getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	@JsonIgnore
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

	public Set<String> getImages() {
		return images;
	}

	public void setImages(Set<String> images) {
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
