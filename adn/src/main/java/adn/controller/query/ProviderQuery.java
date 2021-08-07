/**
 * 
 */
package adn.controller.query;

import adn.controller.query.filter.UUIDFilter;

/**
 * @author Ngoc Huy
 *
 */
public class ProviderQuery extends FactorQuery {

	private UUIDFilter id;

	public UUIDFilter getId() {
		return id;
	}

	public void setId(UUIDFilter id) {
		this.id = id;
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty() && id == null;
	}

}
