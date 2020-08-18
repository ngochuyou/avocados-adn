/**
 * 
 */
package adn.model.specification;

import org.hibernate.SessionFactory;

import adn.application.ApplicationContextProvider;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface TransactionalSpecification<T extends Entity> extends Specification<T> {

	final SessionFactory sessionFactory = ApplicationContextProvider.getApplicationContext()
			.getBean(SessionFactory.class);

}
