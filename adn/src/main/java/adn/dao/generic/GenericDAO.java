/**
 * 
 */
package adn.dao.generic;

import adn.model.entities.Entity;

/**
 * An interface which it's implementations manage the {@link Entity} constraint,
 * specification, and core procedure for each entity interaction.
 * 
 * @author Ngoc Huy
 *
 */
public interface GenericDAO<T extends Entity> {

	default T defaultBuild(T model) {
		return model;
	};

	default T insertionBuild(T model) {
		return model;
	};

	default T updateBuild(T model) {
		return model;
	};

	default T deactivationBuild(T model) {
		return model;
	};

}
