/**
 * 
 */
package adn.model.specification;

import java.io.Serializable;

import adn.helpers.EntityUtils;
import adn.model.DatabaseInteractionResult;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface Specification<T extends Entity> {

	default DatabaseInteractionResult<T> isSatisfiedBy(T instance) {
		return isSatisfiedBy(EntityUtils.getIdentifier(instance), instance);
	}
	
	default DatabaseInteractionResult<T> isSatisfiedBy(Serializable id, T instance) {
		return DatabaseInteractionResult.success(instance);
	}

}
