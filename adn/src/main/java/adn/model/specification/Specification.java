/**
 * 
 */
package adn.model.specification;

import java.io.Serializable;

import adn.dao.DatabaseInteractionResult;

/**
 * @author Ngoc Huy
 *
 */
public interface Specification<T> {

	DatabaseInteractionResult<T> isSatisfiedBy(T instance);

	DatabaseInteractionResult<T> isSatisfiedBy(Serializable id, T instance);

}
