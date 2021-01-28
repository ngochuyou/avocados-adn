/**
 * 
 */
package adn.service.resource.persistence.metamodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import adn.application.context.ContextBuilder;
import adn.service.resource.FileResource;
import adn.service.resource.transaction.GlobalResourceManager;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(6)
public final class MetamodelImpl implements Metamodel, ContextBuilder {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@SuppressWarnings("unused")
	private final EntityManager entityManager;

	private Map<Class<?>, ManagedType<?>> managedTypeMap;

	private Map<Class<?>, EntityType<?>> entityTypeMap;

	private Set<ManagedType<?>> managedTypeSet;

	private Set<EntityType<?>> entityTypeSet;

	/**
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * 
	 */
	@Autowired
	public MetamodelImpl(GlobalResourceManager entityManager) {
		// TODO Auto-generated constructor stub
		Assert.notNull(entityManager, "EntityManager cannot be null");
		this.entityManager = entityManager;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void buildAfterStartUp() throws Exception {
		// TODO Auto-generated method stub
		// @formatter:off
		logger.info("[6]Initializing " + this.getClass().getName());
		managedTypeMap = Map.of(
			String.class, new ManagedStringType(),
			FileResource.class, new FileType()
		);
		entityTypeMap = new HashMap<>();
		
		Entry<Class<?>, EntityType<?>> entry;

		for (Object ele: managedTypeMap.entrySet()
				.stream().filter(ele -> ele.getValue() instanceof EntityType).toArray()) {
			entry = (Entry<Class<?>, EntityType<?>>) ele;
			entityTypeMap.put(entry.getKey(), entry.getValue());
			logger.info("Building EntityType of " + entry.getKey() + " with " + entry.getValue().getClass());
			logger.info(String.format("[resource_identifier_name:%s]", entry.getValue().getId(entry.getValue().getIdType().getJavaType()).getName()));
		}
		
		managedTypeSet = managedTypeMap.values().stream().collect(Collectors.toSet());
		entityTypeSet = entityTypeMap.values().stream().collect(Collectors.toSet());
		// @formatter:on
		logger.info("[6]Initializing " + this.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <X> EntityType<X> entity(Class<X> cls) {
		// TODO Auto-generated method stub
		return (EntityType<X>) entityTypeMap.get(cls);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <X> ManagedType<X> managedType(Class<X> cls) {
		// TODO Auto-generated method stub
		return (ManagedType<X>) managedTypeMap.get(cls);
	}

	@Override
	public <X> EmbeddableType<X> embeddable(Class<X> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ManagedType<?>> getManagedTypes() {
		// TODO Auto-generated method stub
		return managedTypeSet;
	}

	@Override
	public Set<EntityType<?>> getEntities() {
		// TODO Auto-generated method stub
		return entityTypeSet;
	}

	@Override
	public Set<EmbeddableType<?>> getEmbeddables() {
		// TODO Auto-generated method stub
		return Set.of();
	}

}