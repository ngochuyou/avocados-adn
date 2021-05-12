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
public class LocalResourceSession extends SessionImpl implements SessionImplementor, ResourceManager, EventSource {

	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public LocalResourceSession() {
		super(ContextProvider.getLocalResourceSessionFactory(),
				new SessionFactoryImpl.SessionBuilderImpl<>(ContextProvider.getLocalResourceSessionFactory()));
		logger.debug(String.format("Creating new instance of [%s]", this.getClass().getName()));
	}

	@SuppressWarnings("unchecked")
	private <T> ResourcePersister<T> locatePersister(Class<T> clazz) {
		return (ResourcePersister<T>) getFactory().getMetamodel().entityPersister(clazz);
	}

	@SuppressWarnings("unchecked")
	private <T> ResourcePersister<T> locatePersister(String name) {
		return (ResourcePersister<T>) getFactory().getMetamodel().entityPersister(name);
	}

}
