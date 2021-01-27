/**
 * 
 */
package adn.service.resource.persistence.metamodel;

import java.io.Serializable;

import javax.persistence.EntityManager;

import adn.application.context.ContextBuilder;
import adn.application.context.ContextProvider;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractResourcePersister implements ResourcePersister, ContextBuilder {

	private final EntityManager entityManager;

	protected ResourceMetamodel metamodel;

	protected final ResourceTuplizer tuplizer;

	/**
	 * 
	 */
	public AbstractResourcePersister(EntityManager entityManager, ResourceTuplizer tuplizer) {
		// TODO Auto-generated constructor stub
		this.entityManager = entityManager;
		this.tuplizer = tuplizer;
	}

	@Override
	public void buildAfterStartUp() throws Exception {
		// TODO Auto-generated method stub
		this.metamodel = ContextProvider.getApplicationContext().getBean(ResourceMetamodel.class);
	}

	@Override
	public Serializable getIdentifier(Object resource) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the metamodel
	 */
	public ResourceMetamodel getMetamodel() {
		return metamodel;
	}

	@Override
	public EntityManager getEntityManager() {
		// TODO Auto-generated method stub
		return entityManager;
	}

}
