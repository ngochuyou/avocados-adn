/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;

import adn.model.entities.Entity;
import adn.model.entities.User;

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
	 * @param model
	 * @return entity {@link Entity}
	 */
	<E extends T> E buildInsertion(Serializable id, E model);

	/**
	 * Occurs only after the validation of an persist process {@link Entity}
	 * succeeds
	 * 
	 * @param model
	 * @return entity {@link Entity}
	 */
	<E extends T> E buildPostValidationOnInsert(Serializable id, E model);

	/**
	 * @see EntityBuilder#insertionBuild(Entity)
	 * 
	 * @param model
	 * @return persisted {@link Entity}
	 */
	<E extends T> E buildUpdate(Serializable id, E model, E persistence);

	String getLoggableName();

	<E extends T> EntityBuilder<E> and(EntityBuilder<E> next);

}
