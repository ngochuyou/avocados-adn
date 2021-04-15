/**
 * 
 */
package adn.model.factory;

import adn.application.context.ContextProvider;
import adn.helpers.ReflectHelper;
import adn.model.entities.Entity;
import adn.model.models.Model;

/**
 * @author Ngoc Huy
 *
 */
public interface EntityExtractor<T extends Entity, M extends Model> {

	final ReflectHelper reflector = ContextProvider.getApplicationContext().getBean(ReflectHelper.class);

	default T extract(M model, T entity) throws NullPointerException {
		return entity;
	};

	default <E extends T> E merge(T model, E target) throws NullPointerException {
		return target;
	};

	@Deprecated
	@SuppressWarnings({ "unchecked", "rawtypes" })
	default EntityExtractor<?, ?> and(EntityExtractor next) {
		return new And(this, next);
	}

	default String getName() {

		return this.getClass().getName();
	}

}

@Deprecated
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
	public <E extends T> E merge(T model, E target) {
		// TODO Auto-generated method stub
		return this.right.merge(model, this.left.merge(model, target));
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return '[' + left.getName() + ", " + right.getName() + ']';
	}

}