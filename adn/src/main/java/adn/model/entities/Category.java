/**
 * 
 */
package adn.model.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import adn.model.entities.generators.CategoryIdGenerator;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Table(name = "categories")
public class Category extends Factor {

	@Id
	@GeneratedValue(generator = CategoryIdGenerator.NAME)
	@GenericGenerator(name = CategoryIdGenerator.NAME, strategy = CategoryIdGenerator.PATH)
	@Column(updatable = false, length = 5, columnDefinition = "VARCHAR(5)")
	private String id;

	@Column
	private String description;

	@OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
	@JsonIgnore
	private List<Product> products;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

	public String getId() {
		return id;
	}

	public void setId(String code) {
		this.id = code;
	}

}
