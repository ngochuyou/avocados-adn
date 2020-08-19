package adn.model.factory.production.security;

import org.springframework.stereotype.Component;

import adn.model.Genetized;
import adn.model.entities.Entity;
import adn.model.models.Model;
import adn.security.SecuredFor;
import adn.utilities.Role;

@Component
@Genetized(modelGene = Model.class)
public class ModelProducer<T extends Entity, M extends Model> implements AuthenticationBasedModelProducer<T, M> {

	/**
	 * {@link Entity} -> {@link Entity} for <code>ADMIN</code> Authentication
	 */
	@SecuredFor(role = Role.ADMIN)
	public M produceForAdminAuthentication(T entity, M model) {
		// TODO Auto-generated method stub
		model.setCreatedDate(entity.getCreatedDate());
		model.setUpdatedDate(entity.getUpdatedDate());
		model.setActive(entity.isActive());
		model.setDeactivatedDate(entity.getDeactivatedDate());

		return model;
	}

	/**
	 * {@link Entity} -> {@link Entity} for <code>PERSONNEL</code> Authentication
	 */
	@SecuredFor(role = Role.PERSONNEL)
	public M produceForPersonnelAuthentication(T entity, M model) {
		// TODO Auto-generated method stub
		return this.produceForAdminAuthentication(entity, model);
	}

	/**
	 * {@link Entity} -> {@link Entity} for <code>CUSTOMER</code> Authentication
	 */
	@SecuredFor(role = Role.CUSTOMER)
	public M produceForCustomerAuthentication(T entity, M model) {
		// TODO Auto-generated method stub
		return this.produceForAdminAuthentication(entity, model);
	}

}
