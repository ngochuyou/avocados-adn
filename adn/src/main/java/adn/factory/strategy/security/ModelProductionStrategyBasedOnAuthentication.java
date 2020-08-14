package adn.factory.strategy.security;

import java.lang.reflect.InvocationTargetException;

import adn.factory.strategy.ModelProductionStrategy;
import adn.model.Entity;
import adn.model.Model;

/**
 * @author Ngoc Huy
 *
 * @param <M>
 * @param <E>
 */
/**
 * @author Ngoc Huy
 *
 * @param <M>
 * @param <E>
 */
/**
 * @author Ngoc Huy
 *
 * @param <M>
 * @param <E>
 */
public interface ModelProductionStrategyBasedOnAuthentication<M extends Model, E extends Entity>
		extends ModelProductionStrategy<M, E> {

	/**
	 * {@link Entity} -> {@link Model} for <code>ANONYMOUS</code> Authentication
	 */
	@Override
	default M produce(E entity, Class<M> targetModelClass) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		// TODO Auto-generated method stub
		M model = targetModelClass.getConstructor().newInstance();

		model.setId(entity.getId().toString());
		model.setDeactivatedDate(null);

		return model;
	}

	/**
	 * {@link Entity} -> {@link Model} for <code>ADMIN</code> Authentication
	 */
	default M produceForAdminAuthentication(E entity, Class<M> targetModelClass)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		// TODO Auto-generated method stub
		M model = targetModelClass.getConstructor().newInstance();

		model.setId(entity.getId().toString());
		model.setDeactivatedDate(entity.getDeactivatedDate());
		model.setCreatedDate(entity.getCreatedDate());
		model.setUpdatedDate(entity.getUpdatedDate());
		model.setActive(entity.isActive());

		return model;
	}

	default M produceForPersonnelAuthentication(E entity, Class<M> targetModelClass)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		// TODO Auto-generated method stub
		return this.produceForAdminAuthentication(entity, targetModelClass);
	}

	default M produceForCustomerAuthentication(E entity, Class<M> targetModelClass)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		// TODO Auto-generated method stub
		return this.produceForAdminAuthentication(entity, targetModelClass);
	}

}