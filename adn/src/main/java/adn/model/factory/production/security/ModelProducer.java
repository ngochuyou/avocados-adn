package adn.model.factory.production.security;

import adn.model.Entity;
import adn.model.Model;

public class ModelProducer<M extends Model, E extends Entity>
		implements ModelProducerBasedOnAuthentication<M, E> {

	/**
	 * {@link Entity} -> {@link Model} for <code>ANONYMOUS</code> Authentication
	 */
	@Override
	public M produce(E entity, M model) {
		// TODO Auto-generated method stub
		model.setId(entity.getId().toString());
		model.setDeactivatedDate(null);

		return model;
	}

	/**
	 * {@link Entity} -> {@link Model} for <code>ADMIN</code> Authentication
	 */
	public M produceForAdminAuthentication(E entity, M model) {
		// TODO Auto-generated method stub
		model.setId(entity.getId().toString());
		model.setDeactivatedDate(entity.getDeactivatedDate());
		model.setCreatedDate(entity.getCreatedDate());
		model.setUpdatedDate(entity.getUpdatedDate());
		model.setActive(entity.isActive());

		return model;
	}

	/**
	 * {@link Entity} -> {@link Model} for <code>PERSONNEL</code> Authentication
	 */
	public M produceForPersonnelAuthentication(E entity, M model) {
		// TODO Auto-generated method stub
		return this.produceForAdminAuthentication(entity, model);
	}

	/**
	 * {@link Entity} -> {@link Model} for <code>CUSTOMER</code> Authentication
	 */
	public M produceForCustomerAuthentication(E entity, M model) {
		// TODO Auto-generated method stub
		return this.produceForAdminAuthentication(entity, model);
	}

}
