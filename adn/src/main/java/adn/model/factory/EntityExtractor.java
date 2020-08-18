/**
 * 
 */
package adn.model.factory;

import adn.model.entities.Entity;
import adn.model.models.Model;

/**
 * @author Ngoc Huy
 *
 */
public interface EntityExtractor<E extends Entity, M extends Model> {

	default E extract(M model, E entity) {
		return entity;
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	default EntityExtractor<?, ?> and(EntityExtractor next) {
		return new And(this, next);
	}

	default String getName() {

		return this.getClass().getName();
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

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return '[' + left.getName() + ',' + right.getName() + ']';
	}

}