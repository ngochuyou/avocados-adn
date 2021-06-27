package adn.model.factory.production.security;

import adn.model.AbstractModel;
import adn.model.entities.Entity;
import adn.model.factory.ModelProducer;
import adn.model.models.Model;

/**
 * @author Ngoc Huy
 *
 * @param <M>
 * @param <E>
 */
public interface AuthenticationBasedModelProducer<T extends Entity, M extends AbstractModel>
		extends ModelProducer<T, M> {

	/**
	 * {@link Entity} -> {@link Entity} for <code>ANONYMOUS</code> Authentication
	 */
	@Override
	default M produceForAnonymous(T entity, M model) {
		return model;
	}

	/**
	 * {@link Entity} -> {@link Entity} for <code>ADMIN</code> Authentication
	 */
	default M produceForAdminAuthentication(T entity, M model) {
		return model;
	};

	/**
	 * {@link Entity} -> {@link Entity} for <code>PERSONNEL</code> Authentication
	 */
	default M produceForPersonnelAuthentication(T entity, M model) {
		return model;
	};

	/**
	 * {@link Entity} -> {@link Entity} for <code>CUSTOMER</code> Authentication
	 */
	default M produceForCustomerAuthentication(T entity, M model) {
		return model;
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	default public AuthenticationBasedModelProducer<?, ?> and(AuthenticationBasedModelProducer<?, ?> next) {
		return new CompositeAuthenticationBasedProducer(this, next);
	}

}

class CompositeAuthenticationBasedProducer<T extends Entity, M extends Model>
		implements AuthenticationBasedModelProducer<T, M> {

	protected AuthenticationBasedModelProducer<T, M> left;

	protected AuthenticationBasedModelProducer<T, M> right;

	public CompositeAuthenticationBasedProducer(AuthenticationBasedModelProducer<T, M> left,
			AuthenticationBasedModelProducer<T, M> right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	public M produceForAnonymous(T entity, M model) {
		// TODO Auto-generated method stub
		return this.right.produceForAnonymous(entity, this.left.produceForAnonymous(entity, model));
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