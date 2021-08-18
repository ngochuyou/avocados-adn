/**
 * 
 */
package adn.controller.query.specification;

import adn.controller.query.filter.StringFilter;

/**
 * @author Ngoc Huy
 *
 */
public class ProductQuery extends FactorQuery {

	private StringFilter id;

	public StringFilter getId() {
		return id;
	}

	public void setId(StringFilter id) {
		this.id = id;
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty() && id == null;
	}

}