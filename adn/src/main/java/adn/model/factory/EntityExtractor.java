/**
 * 
 */
package adn.model.factory;

import adn.model.Entity;
import adn.model.Model;

/**
 * @author Ngoc Huy
 *
 */
public interface EntityExtractor<E extends Entity, M extends Model> {

	E extract(M model, E entity);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	default EntityExtractor<?, ?> and(EntityExtractor next) {
		return new And(this, next);
	}

}

class And<E extends Entity, M extends Model> implements EntityExtractor<E, M> {

	EntityExtractor<E, M> left;

	EntityExtractor<E, M> right;

	public And(EntityExtractor<E, M> left, EntityExtractor<E, M> right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	public E extract(M model, E entity) {
		// TODO Auto-generated method stub
		return this.right.extract(model, this.left.extract(model, entity));
	}

}