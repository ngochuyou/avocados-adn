/**
 * 
 */
package adn.controller.query.specification;

import adn.controller.query.RestQuery;
import adn.controller.query.filter.StringFilter;

/**
 * @author Ngoc Huy
 *
 */
public class FactorQuery implements RestQuery {

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
