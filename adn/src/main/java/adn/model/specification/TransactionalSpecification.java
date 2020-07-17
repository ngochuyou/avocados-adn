/**
 * 
 */
package adn.model.specification;

import org.hibernate.SessionFactory;

import adn.application.ApplicationContextProvider;
import adn.model.Model;

/**
 * @author Ngoc Huy
 *
 */
public interface TransactionalSpecification<T extends Model> extends Specification<T> {

	final SessionFactory sessionFactory = ApplicationContextProvider.getApplicationContext()
			.getBean(SessionFactory.class);

}
