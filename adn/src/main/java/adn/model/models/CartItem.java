/**
 * 
 */
package adn.model.models;

import java.math.BigInteger;

import adn.model.entities.Product;
import adn.model.entities.constants.NamedSize;

/**
 * @author Ngoc Huy
 *
 */
public class CartItem {

	private static final String LOGGING_TEMPLATE = "Product ID %d with the color %s, size %s";
	public static final String _quantity = "quantity";

	private Product product;

	private BigInteger productId;

	private String color;

	private NamedSize namedSize;

	private Integer quantity;

	public BigInteger getProductId() {
		return productId;
	}

	public String getColor() {
		return color;
	}

	public NamedSize getNamedSize() {
		return namedSize;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setProductId(BigInteger productId) {
		this.productId = productId;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public void setNamedSize(NamedSize namedSize) {
		this.namedSize = namedSize;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
		setProductId(product.getId());
	}

	@Override
	public String toString() {
		return String.format(LOGGING_TEMPLATE, productId, color, namedSize);
	}

}
