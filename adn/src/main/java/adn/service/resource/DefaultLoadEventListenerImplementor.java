/**
 * 
 */
package adn.service.resource;

import org.hibernate.event.internal.DefaultLoadEventListener;
import org.hibernate.event.spi.LoadEvent;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class DefaultLoadEventListenerImplementor extends DefaultLoadEventListener {

	@Override
	protected EntityPersister getPersister(LoadEvent event) {
		return ((LocalResourceSession) event.getSession()).getEntityPersister(event.getEntityClassName(),
				event.getInstanceToLoad());
	}

}
