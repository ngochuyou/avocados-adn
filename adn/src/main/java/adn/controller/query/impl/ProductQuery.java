/**
 * 
 */
package adn.controller.query.impl;

import adn.controller.query.filter.BigDecimalFilter;
import adn.controller.query.filter.BigIntFilter;
import adn.helpers.CollectionHelper;
import adn.model.entities.Product;

/**
 * @author Ngoc Huy
 *
 */
public class ProductQuery extends AbstractFullyAuditedEntityQuery<Product> {

	private BigDecimalFilter price;

	public ProductQuery() {
		super(Product.class, CollectionHelper.emptyHashSet());
	}

	private BigIntFilter id;

	public BigIntFilter getId() {
		return id;
	}

	public void setId(BigIntFilter id) {
		this.id = id;
	}

	public BigDecimalFilter getPrice() {
		return price;
	}

	public void setPrice(BigDecimalFilter price) {
		this.price = price;
	}

	@Override
	public boolean hasCriteria() {
		return id != null || super.hasCriteria();
	}

}