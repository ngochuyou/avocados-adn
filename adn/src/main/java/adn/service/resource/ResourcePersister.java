/**
 * 
 */
package adn.service.resource;

import org.hibernate.persister.entity.EntityPersister;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourcePersister<T> extends EntityPersister {

	@SuppressWarnings("unchecked")
	default <E> E unwrap(Class<E> type) {
		return (E) this;
	}

}
