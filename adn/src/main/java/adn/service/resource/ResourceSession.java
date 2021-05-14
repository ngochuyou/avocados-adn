/**
 * 
 */
package adn.service.resource;

import org.hibernate.FlushMode;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.SessionCreationOptions;
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
public class ResourceSession extends SessionImpl implements SessionImplementor, ResourceManager {

	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static Access access = new Access();
	private static SessionCreationOptions SESSION_CREATION_OPTIONS_FOR_SPRING;

	@Autowired
	public ResourceSession() {
		super(ContextProvider.getLocalResourceSessionFactory(), SESSION_CREATION_OPTIONS_FOR_SPRING);

		logger.debug(String.format("Creating new instance of [%s]", this.getClass().getName()));

		setHibernateFlushMode(FlushMode.MANUAL); // by default
		beginTransaction(); // begin the primary Transaction by default
	}

	@Override
	public ResourcePersister<?> getEntityPersister(String entityName, Object object) {
		return (ResourcePersister<?>) super.getEntityPersister(entityName, object);
	}

	public static class Access {

		private Access() {}

		public void injectSessionCreationOptions(SessionFactoryImpl sfi) {
			SESSION_CREATION_OPTIONS_FOR_SPRING = new SessionFactoryImpl.SessionBuilderImpl<>(sfi);
		}

	}

	public static Access getAccess() throws IllegalAccessException {
		if (access == null) {
			throw new IllegalAccessException(String.format("Access to [%s] was closed", SessionCreationOptions.class));
		}
		return access;
	}

}
