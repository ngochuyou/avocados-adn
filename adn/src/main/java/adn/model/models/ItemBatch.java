/**
 * 
 */
package adn.model.models;

import java.util.Collection;

import adn.model.entities.Item;

public class ItemBatch {

	private Collection<Item> items;

	public Collection<Item> getItems() {
		return items;
	}

	public void setItems(Collection<Item> details) {
		this.items = details;
	}

}