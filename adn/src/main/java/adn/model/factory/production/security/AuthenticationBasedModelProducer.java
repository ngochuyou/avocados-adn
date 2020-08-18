package adn.model.factory.production.security;

import adn.model.entities.Entity;
import adn.model.factory.ModelProducer;
import adn.model.models.Model;

/**
 * @author Ngoc Huy
 *
 * @param <M>
 * @param <E>
 */
public interface AuthenticationBasedModelProducer<M extends Model, E extends Entity> extends ModelProducer<M, E> {

	/**
	 * {@link Entity} -> {@link Model} for <code>ANONYMOUS</code> Authentication
	 */
	@Override
	default M produce(E entity, M model) {
		// TODO Auto-generated method stub
		return model;
	}

	/**
	 * {@link Entity} -> {@link Model} for <code>ADMIN</code> Authentication
	 */
	default M produceForAdminAuthentication(E entity, M model) {
		return model;
	};

	/**
	 * {@link Entity} -> {@link Model} for <code>PERSONNEL</code> Authentication
	 */
	default M produceForPersonnelAuthentication(E entity, M model) {
		return model;
	};

	/**
	 * {@link Entity} -> {@link Model} for <code>CUSTOMER</code> Authentication
	 */
	default M produceForCustomerAuthentication(E entity, M model) {
		return model;
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	default public AuthenticationBasedModelProducer<?, ?> and(AuthenticationBasedModelProducer<?, ?> next) {

		return new CompositeAuthenticationBasedProducer(this, next);
	}

}

class CompositeAuthenticationBasedProducer<T extends Entity, M extends Model>
		implements AuthenticationBasedModelProducer<M, T> {

	protected AuthenticationBasedModelProducer<M, T> left;

	protected AuthenticationBasedModelProducer<M, T> right;

	public CompositeAuthenticationBasedProducer(AuthenticationBasedModelProducer<M, T> left,
			AuthenticationBasedModelProducer<M, T> right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	public M produce(T entity, M model) {
		// TODO Auto-generated method stub
		return this.right.produce(entity, this.left.produce(entity, model));
	}

	@Override
	public M produceForAdminAuthentication(T entity, M model) {
		// TODO Auto-generated method stub
		return this.right.produceForAdminAuthentication(entity, this.left.produceForAdminAuthentication(entity, model));
	}

	@Override
	public M produceForCustomerAuthentication(T entity, M model) {
		// TODO Auto-generated method stub
		return this.right.produceForCustomerAuthentication(entity,
				this.left.produceForCustomerAuthentication(entity, model));
	}

	@Override
	public M produceForPersonnelAuthentication(T entity, M model) {
		// TODO Auto-generated method stub
		return this.right.produceForPersonnelAuthentication(entity,
				this.left.produceForPersonnelAuthentication(entity, model));
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return '[' + this.left.getName() + ", " + this.right.getName() + ']';
	}

}