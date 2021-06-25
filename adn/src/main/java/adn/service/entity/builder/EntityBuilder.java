/**
 * 
 */
package adn.service.entity.builder;

import adn.model.entities.Account;
import adn.model.entities.Entity;

/**
 * An interface whose implementation manage {@link Entity} constraints,
 * specifications, core procedures, etc.. for each entity interaction.
 * </p>
 * 
 * @author Ngoc Huy
 *
 */
public interface EntityBuilder<T extends Entity> {

	/**
	 * Occurs only when the entity is being inserted
	 * </p>
	 * <em>Example:</em> While inserting an {@link Account} we always hash the
	 * password. Whereas an update has to perform a check to determine if the user
	 * is updating their password or not, then make the decision to hash/update that
	 * password
	 * 
	 * @param entity
	 * @return entity {@link Entity}
	 */
	default T insertionBuild(T entity) {
		return entity;
	};

	/**
	 * @see EntityBuilder#insertionBuild(Entity)
	 * 
	 * @param entity
	 * @return persisted {@link Entity}
	 */
	default T updateBuild(T entity) {
		return entity;
	};

	/**
	 * <em>Example:</em> Set the deactivated time-stamp
	 * 
	 * @see EntityBuilder#insertionBuild(Entity)
	 * 
	 * @param entity
	 * @return
	 */
	default T deactivationBuild(T entity) {
		return entity;
	};

}
