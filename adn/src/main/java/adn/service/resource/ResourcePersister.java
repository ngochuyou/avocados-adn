/**
 * 
 */
package adn.service.resource;

import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.property.access.spi.PropertyAccess;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourcePersister<T> extends EntityPersister {

	@SuppressWarnings("unchecked")
	default <E> E unwrap(Class<E> type) {
		return (E) this;
	}

	PropertyAccess getPropertyAccess(String propertyName);

	PropertyAccess getPropertyAccess(int propertyIndex);

}
