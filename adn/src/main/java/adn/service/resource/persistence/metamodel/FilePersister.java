/**
 * 
 */
package adn.service.resource.persistence.metamodel;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import adn.service.resource.transaction.GlobalResourceManager;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(7)
public class FilePersister extends AbstractResourcePersister {

	/**
	 * @param entityManager
	 */
	@Autowired
	public FilePersister(@Autowired GlobalResourceManager entityManager) {
		super(entityManager, null);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Serializable getIdentifier(Object resource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityManager getEntityManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEntityName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceTuplizer getTuplizer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isVersioned() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setIdentifier(Object resource, Serializable id)
			throws IllegalAccessException, IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public Object instantiate(Serializable id) throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getVersion(Object resource) throws IllegalAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPropertyValues(Object resource, Object[] values)
			throws IllegalAccessException, IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPropertyValue(Object resource, int i, Object value)
			throws IllegalAccessException, IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getPropertyValues(Object resource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPropertyValue(Object resource, int i) throws IllegalAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPropertyValue(Object resource, String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

}
