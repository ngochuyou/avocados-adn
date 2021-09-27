/**
 * 
 */
package adn.model.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import adn.model.entities.generators.CategoryCodeGenerator;
import adn.model.entities.metadata._Category;
import adn.model.entities.metadata._Product;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Table(name = "categories")
public class Category extends PermanentEntity implements NamedResource {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;

	@Column(updatable = false, unique = true, length = _Category.CODE_LENGTH)
	@GeneratedValue(generator = CategoryCodeGenerator.NAME)
	@GenericGenerator(name = CategoryCodeGenerator.NAME, strategy = CategoryCodeGenerator.PATH)
	private String code;

	@Column(nullable = false, unique = true)
	private String name;

	@Column
	private String description;

	@OneToMany(mappedBy = _Product.category, fetch = FetchType.LAZY)
	@JsonIgnore
	private List<Product> products;

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

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

}
