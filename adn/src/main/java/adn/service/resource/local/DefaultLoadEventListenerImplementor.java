/**
 * 
 */
package adn.service.resource.local;

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
		// TODO Auto-generated method stub
		return ((LocalResourceSession) event.getSession()).getMetamodel()
				.getResourcePersister(event.getEntityClassName());
	}

}
