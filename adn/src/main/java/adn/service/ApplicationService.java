/**
 * 
 */
package adn.service;

import adn.model.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface ApplicationService<T extends Entity> {

	default T doProcedure(T model) {
		return model;
	};

	default T doInsertionProcedure(T model) {
		return model;
	};

	default T doUpdateProcedure(T model) {
		return model;
	};

	default T doDeactivationProcedure(T model) {
		return model;
	};

}
