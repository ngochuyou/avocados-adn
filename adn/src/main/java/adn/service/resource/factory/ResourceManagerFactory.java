/**
 * 
 */
package adn.service.resource.factory;

import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.internal.FastSessionServices;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.service.ServiceRegistry;
import org.springframework.util.Assert;

import adn.service.resource.engine.Storage;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceManagerFactory extends SessionFactoryImpl implements EntityManagerFactoryImplementor {

	private static final long serialVersionUID = 1L;

	private final Storage localStorage;

	public static final String DTYPE_COLUMNNAME = "DTYPE";
	public static final String DTYPE_SEPERATOR = "_";

	public ResourceManagerFactory(
	// @formatter:off
		final Storage localStorage,
		final MetadataImplementor metadata,
		final ServiceRegistry serviceRegistry,
		final SessionFactoryOptions sessionFactoryOptions,
		final FastSessionServices fsses) throws IllegalAccessException {
	// @formatter:on
		super(metadata, sessionFactoryOptions, HQLQueryPlan::new);

		Assert.notNull(localStorage, String.format("[%s] must not be null", Storage.class));
		this.localStorage = localStorage;
	}

	@Override
	public Storage getStorage() {
		return localStorage;
	}

}
