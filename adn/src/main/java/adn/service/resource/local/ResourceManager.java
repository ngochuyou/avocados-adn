/**
 * 
 */
package adn.service.resource.local;

import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceManager extends SharedSessionContractImplementor {

	ResourceManagerFactory getResourceManagerFactory();

	void setRollbackOnly();

	boolean isRollbackOnly();

	@SuppressWarnings("unchecked")
	default <E extends ResourceManager> E unwrapManager(Class<? super E> type) {
		return (E) this;
	}

}
