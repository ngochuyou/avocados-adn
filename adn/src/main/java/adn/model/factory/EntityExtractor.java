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
public interface EntityExtractor<T extends Entity, M extends Model> {

	default T extract(M model, T entity) {
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

class And<T extends Entity, M extends Model> implements EntityExtractor<T, M> {

	EntityExtractor<T, M> left;

	EntityExtractor<T, M> right;

	public And(EntityExtractor<T, M> left, EntityExtractor<T, M> right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	public T extract(M model, T entity) {
		// TODO Auto-generated method stub
		return this.right.extract(model, this.left.extract(model, entity));
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return '[' + left.getName() + ", " + right.getName() + ']';
	}

}