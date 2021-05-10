/**
 * 
 */
package adn.service.resource;

import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * @author Ngoc Huy
 *
 */
public interface SharedSessionUnwrapper {

	default ResourceManager unwrapSession(SharedSessionContractImplementor session) {
		return (ResourceManager) session;
	}

}
