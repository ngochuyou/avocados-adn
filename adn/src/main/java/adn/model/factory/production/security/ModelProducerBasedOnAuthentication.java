package adn.model.factory.production.security;

import adn.model.Entity;
import adn.model.Model;
import adn.model.factory.ModelProducer;

/**
 * @author Ngoc Huy
 *
 * @param <M>
 * @param <E>
 */
public interface ModelProducerBasedOnAuthentication<M extends Model, E extends Entity>
		extends ModelProducer<M, E> {

	/**
	 * {@link Entity} -> {@link Model} for <code>ANONYMOUS</code> Authentication
	 */
	@Override
	default M produce(E entity, M model) {
		return model;
	};

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

}