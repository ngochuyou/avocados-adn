/**
 * 
 */
package adn.model.entities.validator;

import java.io.Serializable;

import org.hibernate.Session;

import adn.dao.generic.Result;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface Validator<T extends Entity> {

	Result<T> isSatisfiedBy(Session session, T instance);

	Result<T> isSatisfiedBy(Session session, Serializable id, T instance);

	default String getLoggableName() {
		return this.getClass().getSimpleName();
	}

	<E extends T> Validator<E> and(Validator<E> next);

}
