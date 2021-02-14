/**
 * 
 */
package adn.service.resource.tuple;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.metamodel.Attribute;

import org.springframework.util.Assert;

import adn.service.resource.metamodel.ResourceMetamodel;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractResourceTuplizer implements ResourceTuplizer {

	private final ResourceMetamodel metamodel;

	private final Getter idGetter;
	private final Setter idSetter;

	protected final Getter[] getters;
	protected final Setter[] setters;
	protected final int propertySpan;

	private final Instantiator instantiator;

	/**
	 * 
	 */
	public AbstractResourceTuplizer(ResourceMetamodel metamodel) {
		// TODO Auto-generated constructor stub
		Assert.notNull(metamodel, "resourceMetamodel cannot be null");

		this.metamodel = metamodel;

		boolean isVirtualId = metamodel.getIdentifierAttribute().isVirtual();

		idGetter = !isVirtualId ? buildPropertyGetter(metamodel.getIdentifierAttribute()) : null;
		idSetter = !isVirtualId ? buildPropertySetter(metamodel.getIdentifierAttribute()) : null;
		propertySpan = metamodel.getPropertySpan();
		getters = new Getter[propertySpan];
		setters = new Setter[propertySpan];

		Attribute<?, ?>[] attributes = metamodel.getProperties();

		for (int i = 0; i < propertySpan; i++) {
			getters[i] = buildPropertyGetter(attributes[i]);
			setters[i] = buildPropertySetter(attributes[i]);
		}

		instantiator = buildInstantiator(metamodel);
	}

	protected abstract <X, Y> Getter buildPropertyGetter(Attribute<X, Y> attribute);

	protected abstract <X, Y> Setter buildPropertySetter(Attribute<X, Y> attribute);

	protected abstract Instantiator buildInstantiator(ResourceMetamodel metamodel);

	@Override
	public Serializable getIdentifier(Object resource, EntityManager resoureManager) throws PersistenceException {
		// TODO Auto-generated method stub
		try {
			return (Serializable) idGetter.get(resource);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new PersistenceException(e);
		}
	}

	@Override
	public void setIdentifier(Object resource, Serializable id, EntityManager resourceManager)
			throws PersistenceException {
		// TODO Auto-generated method stub
		try {
			if (metamodel.getIdentifierProperty().isEmbedded()) {
				if (resource != null) {
					// TODO: type exclusively set the composite identifier
					return;
				}
			}

			if (idSetter != null) {
				idSetter.set(resource, idGetter.get(resource));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new PersistenceException(e);
		}
	}

	@Override
	public Object getVersion(Object resource) throws PersistenceException {
		// TODO Auto-generated method stub
		if (!metamodel.isVersioned()) {
			return null;
		}

		try {
			return getters[metamodel.getVersionPropertyIndex()].get(resource);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new PersistenceException(e);
		}
	}

	@Override
	public Object getPropertyValue(Object resource, String propertyName) throws PersistenceException {
		// TODO Auto-generated method stub
		try {
			return getters[metamodel.getPropertyIndexes().get(propertyName)].get(resource);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new PersistenceException(e);
		}
	}

	@Override
	public Object[] getPropertyValues(Object resource) throws PersistenceException {
		// TODO Auto-generated method stub
		int span = propertySpan;
		Object[] result = new Object[span];

		for (int i = 0; i < span; i++) {
			try {
				result[i] = getters[i].get(resource);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				throw new PersistenceException(e);
			}
		}

		return result;
	}

	@Override
	public void setPropertyValue(Object resource, int i, Object value) throws PersistenceException {
		// TODO Auto-generated method stub
		try {
			setters[i].set(resource, value);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new PersistenceException(e);
		}
	}

	@Override
	public void setPropertyValue(Object resource, String propertyName, Object value) throws PersistenceException {
		// TODO Auto-generated method stub
		try {
			setters[metamodel.getPropertyIndexes().get(propertyName)].set(resource, value);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new PersistenceException(e);
		}
	}

	@Override
	public void setPropertyValues(Object resource, Object[] values) throws PersistenceException {
		// TODO Auto-generated method stub
		try {
			for (int i = 0; i < propertySpan; i++) {
				setters[i].set(resource, values[i]);
			}
		} catch (Exception e) {
			throw new PersistenceException(e);
		}
	}

	@Override
	public Object instantiate(Serializable id, EntityManager resoureManager) throws PersistenceException {
		// TODO Auto-generated method stub
		try {
			return instantiator.instantiate(id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new PersistenceException(e);
		}
	}

	@Override
	public boolean isInstance(Object resource) throws PersistenceException {
		// TODO Auto-generated method stub
		return instantiator.isInstance(resource);
	}

	public ResourceMetamodel getMetamodel() {
		return metamodel;
	}

	@Override
	public void afterInitialize(Object resource, EntityManager resourceManager) {
		// TODO Auto-generated method stub
	}

}
