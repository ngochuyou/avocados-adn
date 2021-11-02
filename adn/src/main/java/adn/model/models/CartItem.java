/**
 * 
 */
package adn.model.models;

import java.math.BigInteger;

import adn.model.entities.constants.NamedSize;

/**
 * @author Ngoc Huy
 *
 */
public class CartItem {

	private static final String LOGGING_TEMPLATE = "Product ID %d with the color %s, size %s";

	private BigInteger productId;

	private String color;

	private NamedSize namedSize;

	private int quantity;

	public BigInteger getProductId() {
		return productId;
	}

	public String getColor() {
		return color;
	}

	public NamedSize getNamedSize() {
		return namedSize;
	}

	public int getQuantity() {
		return quantity;
	}

	@Override
	public String toString() {
		return String.format(LOGGING_TEMPLATE, productId, color, namedSize);
	}

}
