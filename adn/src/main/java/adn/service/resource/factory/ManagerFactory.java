/**
 * 
 */
package adn.service.resource.factory;

import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.internal.FastSessionServices;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import adn.service.resource.storage.LocalResourceStorage;

/**
 * @author Ngoc Huy
 *
 */
public class ManagerFactory extends SessionFactoryImpl implements EntityManagerFactoryImplementor {

	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final LocalResourceStorage localStorage;

	public ManagerFactory(
	// @formatter:off
			final LocalResourceStorage localStorage,
			final MetadataImplementor metadata,
			final ServiceRegistry serviceRegistry,
			final SessionFactoryOptions sessionFactoryOptions,
			final FastSessionServices fsses) throws IllegalAccessException {
		// @formatter:on
		super(metadata, sessionFactoryOptions, null);

		Assert.notNull(localStorage, String.format("[%s] must not be null", LocalResourceStorage.class));
		this.localStorage = localStorage;

		modifyLoadEventListeners();
	}

	private void modifyLoadEventListeners() {
		logger.trace(String.format("Modifying [%s]", getFastSessionServices().eventListenerGroup_LOAD.getClass()));
	}

	@Override
	public LocalResourceStorage getStorage() {
		return localStorage;
	}

}