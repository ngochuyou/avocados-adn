/**
 * 
 */
package adn.service.resource;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import adn.application.context.ContextProvider;

/**
 * @author Ngoc Huy
 *
 */
@Component
@RequestScope
@Lazy
public class ResourceSession extends SessionImpl implements SessionImplementor, ResourceManager, EventSource {

	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public ResourceSession() {
		super(ContextProvider.getLocalResourceSessionFactory(),
				new SessionFactoryImpl.SessionBuilderImpl<>(ContextProvider.getLocalResourceSessionFactory()));
		logger.debug(String.format("Creating new instance of [%s]", this.getClass().getName()));
	}

	@Override
	public ResourcePersister<?> getEntityPersister(String entityName, Object object) {
		return (ResourcePersister<?>) super.getEntityPersister(entityName, object);
	}

}
