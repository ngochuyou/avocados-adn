/**
 * 
 */
package adn.model.specification;

import org.hibernate.SessionFactory;

import adn.application.ContextProvider;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface TransactionalSpecification<T extends Entity> extends Specification<T> {

	final SessionFactory sessionFactory = ContextProvider.getApplicationContext()
			.getBean(SessionFactory.class);

}
