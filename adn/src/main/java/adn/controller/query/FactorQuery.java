/**
 * 
 */
package adn.controller.query;

import adn.controller.query.filter.StringFilter;

/**
 * @author Ngoc Huy
 *
 */
public class FactorQuery extends AbstractRestQuery {

	private StringFilter name;

	public StringFilter getName() {
		return name;
	}

	public void setName(StringFilter name) {
		this.name = name;
	}
	
	@Override
	public boolean isEmpty() {
		return name == null;
	}
	
}
