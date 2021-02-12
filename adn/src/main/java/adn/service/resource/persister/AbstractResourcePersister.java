/**
 * 
 */
package adn.service.resource.persister;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.service.resource.ImmutableResourceEntryFactory;
import adn.service.resource.MutableResourceEntryFactory;
import adn.service.resource.ResourceEntryFactory;
import adn.service.resource.metamodel.ClassMetadata;
import adn.service.resource.metamodel.EntityMode;
import adn.service.resource.metamodel.ResourceMetamodel;
import adn.service.resource.metamodel.ResourceTypeImpl;
import adn.service.resource.tuple.ResourceTuplizer;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractResourcePersister implements ResourcePersister, ClassMetadata, Lockable {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final EntityManager resourceManager;

	private final ResourceMetamodel metamodel;
	private final ResourceTuplizer tuplizer;
	private final ResourceEntryFactory entryFactory;

	/**
	 * 
	 */
	public <X> AbstractResourcePersister(ResourceTypeImpl<X> type, EntityManager resourceManager) {
		// TODO Auto-generated constructor stub
		this.resourceManager = resourceManager;
		metamodel = new ResourceMetamodel(type, this, this.resourceManager);
		tuplizer = metamodel.getTuplizer();
		entryFactory = metamodel.isMutable() ? MutableResourceEntryFactory.INSTANCE
				: ImmutableResourceEntryFactory.INSTANCE;
	}

	/**
	 * @return the resourceManager
	 */
	@Override
	public EntityManager getResourceManager() {
		return resourceManager;
	}

	@Override
	public ResourceEntryFactory getEntryFactory() {
		// TODO Auto-generated method stub
		return entryFactory;
	}

	@Override
	public EntityMode getEntityMode() {
		// TODO Auto-generated method stub
		return metamodel.getMode();
	}

}
