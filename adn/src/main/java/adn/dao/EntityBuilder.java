/**
 * 
 */
package adn.dao;

import adn.model.entities.Account;
import adn.model.entities.Entity;

/**
 * An interface whose implementation manage {@link Entity} constraints,
 * specifications, core procedures, etc.. for each entity interaction.
 * </p>
 * All methods follow specific principal where they receive an non-persisted
 * {@link Entity} and produce a persisted {@link Entity} whose values were
 * merged from the non-persisted instance. This process is a top-down pattern.
 * </p>
 * However, as part of the contract, these methods do not fetch the persisted
 * {@link Entity} as it has to be separately fetched by which ever unit is
 * calling them. I.E: the {@link BaseDAO#insert(Entity, Class)}. This makes them
 * part away from the DAO concept and stay closer to the service business.
 * 
 * @author Ngoc Huy
 *
 */
public interface EntityBuilder<T extends Entity> {

	/**
	 * This process occurs in every cases(insert, update, etc...). It's a shared
	 * business
	 * </p>
	 * <em>Example:</em> A persisted {@link Account#email}, whether being inserted
	 * or updated..., always need the email information so that any requested
	 * informations will reflect in the database
	 * 
	 * @param model
	 * @return persisted {@link Entity}
	 */
	default T defaultBuild(final T model) {
		return model;
	};

	/**
	 * Occurs only when the persistence is being inserted
	 * </p>
	 * <em>Example:</em> While inserting an {@link Account} we always hash the
	 * password. Whereas an update has to perform a check to determine if the user
	 * is updating their password or not, then make the decision to hash/update that
	 * password
	 * 
	 * @param model
	 * @return persisted {@link Entity}
	 */
	default T insertionBuild(final T model) {
		return model;
	};

	/**
	 * @see EntityBuilder#insertionBuild(Entity)
	 * 
	 * @param model
	 * @return persisted {@link Entity}
	 */
	default T updateBuild(final T model) {
		return model;
	};

	/**
	 * <em>Example:</em> Set the deactivated time-stamp
	 * 
	 * @see EntityBuilder#insertionBuild(Entity)
	 * 
	 * @param model
	 * @return
	 */
	default T deactivationBuild(final T model) {
		return model;
	};

}
