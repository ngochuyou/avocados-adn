/**
 * 
 */
package adn.model.factory.authentication;

import adn.model.DomainEntity;
import adn.model.factory.authentication.dynamicmap.DynamicMapModelProducer;

/**
 * @author Ngoc Huy
 *
 */
public interface DynamicMapModelProducerFactory {

	<T extends DomainEntity> DynamicMapModelProducer<T> getProducers(Class<T> entityType);

}
