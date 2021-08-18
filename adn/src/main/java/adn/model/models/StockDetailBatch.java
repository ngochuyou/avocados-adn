/**
 * 
 */
package adn.model.models;

import java.util.Collection;

import adn.model.entities.StockDetail;

public class StockDetailBatch {

	private Collection<StockDetail> details;

	public Collection<StockDetail> getDetails() {
		return details;
	}

	public void setDetails(Collection<StockDetail> details) {
		this.details = details;
	}

}