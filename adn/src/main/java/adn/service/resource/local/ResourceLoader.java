/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceLoader extends AbstractLoader {

	protected final ResourcePersister<?> persister;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public ResourceLoader(ResourcePersister<?> persister) {
		// TODO Auto-generated constructor stub
		this.persister = persister;
	}

	@Override
	public Object load(Serializable id, Object optionalObject, ResourceManager manager) {
		// TODO Auto-generated method stub
		return load(id, optionalObject, manager, LockOptions.NONE);
	}

	@Override
	public Object load(Serializable id, Object optionalObject, ResourceManager manager, LockOptions lockOptions) {
		// TODO Auto-generated method stub
		assertIdType(id);
		logger.debug(String.format("Loading resource %s", id));

		List<?> result;
		try {
			result = doLoad(id, manager, persister, lockOptions);

			if (result.size() == 0) {
				return null;
			}

			if (result.size() > 1) {
				throw new IllegalStateException("More than one resource were found with identifier " + id);
			}

			logger.debug(String.format("Done loading", id.toString()));

			return result.get(0);
		} catch (HibernateException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private void assertIdType(Serializable id) {
		Class<?> idType = persister.getIdentifierType().getReturnedClass();

		Assert.isTrue(idType.equals(id) || idType.isAssignableFrom(id.getClass()), "Identifier type check failed");
	}

	@Override
	public ResourcePersister<?> getPersister() {
		// TODO Auto-generated method stub
		return persister;
	}

}
