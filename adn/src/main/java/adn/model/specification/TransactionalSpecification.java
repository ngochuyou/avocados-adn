/**
 * 
 */
package adn.model.specification;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import adn.application.context.ContextProvider;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public abstract class TransactionalSpecification<T extends Entity> implements Specification<T> {

	protected Session getCurrentSession() {
		return ContextProvider.getApplicationContext().getBean(SessionFactory.class).getCurrentSession();
	}

}
