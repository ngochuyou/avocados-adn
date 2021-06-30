/**
 * 
 */
package adn.model.specification;

import java.io.Serializable;

import adn.model.DatabaseInteractionResult;

/**
 * @author Ngoc Huy
 *
 */
public interface Specification<T> {

	default DatabaseInteractionResult<T> isSatisfiedBy(T instance) {
		return DatabaseInteractionResult.success(instance);
	}

	default DatabaseInteractionResult<T> isSatisfiedBy(Serializable id, T instance) {
		return DatabaseInteractionResult.success(instance);
	}

}
