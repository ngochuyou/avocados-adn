/**
 * 
 */
package adn.model.specification;

import adn.model.DatabaseInteractionResult;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface Specification<T extends Entity> {

	default DatabaseInteractionResult<T> isSatisfiedBy(T instance) {
		return DatabaseInteractionResult.success(instance);
	}

}
