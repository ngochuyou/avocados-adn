/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;

import adn.model.entities.User;
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
	 * <em>Example:</em> While inserting an {@link User} we always hash the
	 * password. Whereas an update has to perform a check to determine if the user
	 * is updating their password or not, then make the decision to hash/update that
	 * password
	 * 
	 * @param entity
	 * @return entity {@link Entity}
	 */
	<E extends T> E buildInsertion(Serializable id, E entity);

	/**
	 * @see EntityBuilder#insertionBuild(Entity)
	 * 
	 * @param entity
	 * @return persisted {@link Entity}
	 */
	<E extends T> E buildUpdate(Serializable id, E entity, E persistence);

}
