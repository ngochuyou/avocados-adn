/**
 * 
 */
package adn.model.specification;

import java.io.Serializable;

import org.hibernate.Session;

import adn.dao.generic.Result;

/**
 * @author Ngoc Huy
 *
 */
public interface Specification<T> {

	Result<T> isSatisfiedBy(Session session, T instance);

	Result<T> isSatisfiedBy(Session session, Serializable id, T instance);

}
