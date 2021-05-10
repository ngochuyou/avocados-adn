/**
 * 
 */
package adn.service.resource;

import org.hibernate.engine.spi.SharedSessionContractImplementor;

import adn.service.resource.factory.EntityManagerFactoryImplementor;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceManager extends SharedSessionContractImplementor {

	@SuppressWarnings("unchecked")
	default <E extends ResourceManager> E unwrapManager(Class<? super E> type) {
		return (E) this;
	}

	@Override
	EntityManagerFactoryImplementor getFactory();

}
