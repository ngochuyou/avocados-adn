/**
 * 
 */
package adn.controller.query.impl;

import java.util.HashSet;

import adn.controller.query.filter.BooleanFilter;
import adn.model.entities.PermanentEntity;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractPermanentEntityQuery<T extends PermanentEntity> extends AbstractRestQuery<T> {

	private BooleanFilter active;

	public AbstractPermanentEntityQuery(Class<T> entityType, HashSet<String> associationColumns) {
		super(entityType, associationColumns);
	}

	public BooleanFilter getActive() {
		return active;
	}

	public void setActive(BooleanFilter active) {
		this.active = active;
	}

	@Override
	public boolean hasCriteria() {
		return active != null;
	}

}
