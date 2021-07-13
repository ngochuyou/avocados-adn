/**
 * 
 */
package adn.model.entities;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Table(name = "categories")
public class Category extends Factor {

	@Column
	private String description;

	@Column(nullable = false, updatable = false, length = 5, columnDefinition = "NVARCHAR(5)")
	private String code;

	@OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
