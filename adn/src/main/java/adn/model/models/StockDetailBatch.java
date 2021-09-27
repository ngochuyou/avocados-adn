/**
 * 
 */
package adn.model.models;

import java.util.Collection;

import adn.model.entities.Item;

public class StockDetailBatch {

	private Collection<Item> details;

	public Collection<Item> getDetails() {
		return details;
	}

	public void setDetails(Collection<Item> details) {
		this.details = details;
	}

}