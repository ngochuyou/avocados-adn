/**
 * 
 */
package adn.service.resource.metamodel;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;

import org.hibernate.EntityNameResolver;
import org.hibernate.graph.spi.RootGraphImplementor;
import org.hibernate.metamodel.model.domain.internal.SingularAttributeImpl.Identifier;
import org.hibernate.metamodel.model.domain.internal.SingularAttributeImpl.Version;
import org.hibernate.metamodel.model.domain.spi.EmbeddedTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.PersistentAttributeDescriptor;
import org.hibernate.metamodel.model.domain.spi.SingularPersistentAttribute;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.tuple.GenerationTiming;
import org.hibernate.tuple.ValueGeneration;
import org.hibernate.tuple.ValueGenerator;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.type.BasicTypeRegistry;
import org.hibernate.type.spi.TypeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import adn.helpers.ReflectHelper;
import adn.service.resource.local.ContextBuildingService;
import adn.service.resource.local.Metadata;
import adn.service.resource.local.NamingStrategy;
import adn.service.resource.local.ResourcePersister;
import adn.service.resource.local.ResourcePersisterImpl;
import adn.service.resource.local.factory.EntityManagerFactoryImplementor;
import adn.service.resource.metamodel.PropertyBinder.AttributeRole;

/**
 * @author Ngoc Huy
 *
 */
public class MetamodelImpl implements Metamodel, MetamodelImplementor {

	private static final Logger logger = LoggerFactory.getLogger(MetamodelImpl.class);

	private final EntityManagerFactoryImplementor sessionFactory;

	private final Map<String, String> importedClassNames;
	private final Map<String, ResourceType<?>> jpaMetamodels;
	private final Map<String, ResourcePersister<?>> persistersByName;

	private final Set<EntityNameResolver> resourceNameResolvers;

	public MetamodelImpl(ContextBuildingService serviceRegistry, EntityManagerFactoryImplementor sessionFactory) {
		// TODO Auto-generated constructor stub
		Assert.notNull(sessionFactory, "ResourceManagerFactory must not be null");
		this.sessionFactory = sessionFactory;
		this.jpaMetamodels = new HashMap<>();
		this.persistersByName = new HashMap<>();
		this.resourceNameResolvers = new HashSet<>();
		this.importedClassNames = new HashMap<>();
	}

	@Override
	public <X> ResourceType<X> entity(Class<X> cls) {

		return entity(sessionFactory.getContextBuildingService().getService(NamingStrategy.class).getName(cls));
	}

	@SuppressWarnings("unchecked")
	public <X> ResourceType<X> entity(String name) {

		if (!jpaMetamodels.containsKey(name)) {
			return null;
		}

		return (ResourceType<X>) jpaMetamodels.get(name);
	}

	@Override
	public <X> ResourceType<X> managedType(Class<X> cls) {

		return entity(cls);
	}

	@Override
	public <X> EmbeddedTypeDescriptor<X> embeddable(Class<X> cls) {

		return null;
	}

	@Override
	public Set<ManagedType<?>> getManagedTypes() {

		return Collections.unmodifiableSet(new HashSet<>(jpaMetamodels.values()));
	}

	@Override
	public Set<EntityType<?>> getEntities() {

		return Collections.unmodifiableSet(new HashSet<>(jpaMetamodels.values()));
	}

	@Override
	public Set<EmbeddableType<?>> getEmbeddables() {
		return Collections.emptySet();
	}

	@SuppressWarnings("unchecked")
	public <T> ResourcePersister<T> getResourcePersister(String resourceName) {
		if (!persistersByName.keySet().contains(resourceName)) {
			return null;
		}

		return (ResourcePersister<T>) persistersByName.get(resourceName);
	}

	public Set<ResourcePersister<?>> getResourceDescriptors() {
		return Collections.unmodifiableSet(new HashSet<>(persistersByName.values()));
	}

	@Override
	public void prepare() throws PersistenceException {
		BasicTypeRegistry typeRegistry = sessionFactory.getContextBuildingService()
				.getServiceWrapper(BasicTypeRegistry.class, wrapper -> wrapper.orElseThrow().unwrap());

		Assert.notNull(typeRegistry, "BasicTypeRegistry must not be null");
		sessionFactory.getContextBuildingService().register(CentralAttributeContext.class,
				new CentralAttributeContext.CentralAttributeContextImpl(typeRegistry, this));
		resourceNameResolvers.add(sessionFactory.getContextBuildingService().getService(NamingStrategy.class));
	}

	@Override
	public void process() throws PersistenceException {
		try {
			imports();
			postImport();
			resolvePersisters();
		} catch (Exception e) {
			e.printStackTrace();
			throw new PersistenceException(e);
		}
	}

	@Override
	public void postProcess() throws PersistenceException {
		ContextBuildingService contextService = sessionFactory.getContextBuildingService();
		Metadata metadata = contextService.getService(Metadata.class);

		jpaMetamodels.values().forEach(metamodel -> {
			logger.trace("Closing access to " + metamodel.getName() + " metamodel");
			((ResourceType<?>) metamodel).getInFlightAccess().finishUp();
		});

		Assert.isTrue(metadata.getImports().keySet().stream()
				.filter(key -> !metadata.isProcessingDone(key) || entity(key) == null).findAny().orElse(null) == null,
				"Processing is not done, cannot invoke postProcess");
		Assert.isTrue(persistersByName.values().stream().filter(persister -> {
			if (persister.getEntityMetamodel() == null) {
				logger.error(EntityMetamodel.class + " in persister of resource named " + persister.getEntityName()
						+ " was not processed");
			}

			return persister.getEntityMetamodel() == null;
		}).count() == 0, "Some HBN metamodel was not processed");

		logger.trace("Metamodel building summary:\n"
				+ persistersByName.values().stream().map(ele -> ele.toString()).collect(Collectors.joining("\n")));
	}

	private void imports() throws IllegalAccessException {
		ContextBuildingService contextService = sessionFactory.getContextBuildingService();
		Metadata metadata = contextService.getService(Metadata.class);

		Assert.notNull(metadata, "Unable to locate Metadata");

		Map<String, Class<?>> imports = metadata.getImports();
		ModelProcessor modelProcessor = new ModelProcessor(contextService.getService(CentralAttributeContext.class));

		for (Map.Entry<String, Class<?>> entry : imports.entrySet()) {
			modelProcessor.processModel(entry.getKey(), entry.getValue(), null);
		}
	}

	private void postImport() throws IllegalAccessException {
		logger.trace("Post import");

		for (ResourceType<?> metamodel : jpaMetamodels.values()) {
			visitInheritance(metamodel);
		}

		for (ResourceType<?> metamodel : jpaMetamodels.values()) {
			logger.trace("Tracing identifier of type: " + metamodel.getName());
			resolveIdentifierAndVersion(metamodel);
		}
	}

	private void resolvePersisters() {
		logger.trace("Resolving persisters");

		for (ResourceType<?> metamodel : jpaMetamodels.values()) {
			if (metamodel.getSupertype() == null) {
				fromRoot(metamodel, (node) -> {
					ResourcePersister<?> persister;

					persistersByName.put(node.getName(), persister = createPersister(node));
					persister.generateEntityDefinition();
					persister.postInstantiate();
				});
			}
		}
	}

	private <T> void fromRoot(ResourceType<? extends T> root, Consumer<ResourceType<? extends T>> consumer) {
		consumer.accept(root);

		root.getSubclassNames().forEach(name -> fromRoot(entity(name), consumer));
	}

	private <T> ResourcePersister<T> createPersister(ResourceType<T> metamodel) {
		return new ResourcePersisterImpl<>(sessionFactory, metamodel);
	}

	@SuppressWarnings("unchecked")
	private <X> void resolveIdentifierAndVersion(ResourceType<X> metamodel) throws IllegalAccessException {
		Assert.notNull(metamodel, "ResourceType must not be null");

		if (metamodel.hasSingleIdAttribute()) {
			SingularPersistentAttribute<?, ?> rawIdentifier = locateIdentifier(metamodel);

			if (rawIdentifier == null) {
				throw new IllegalArgumentException(
						"Unable to locate Identifier of type: " + metamodel.getJavaType().getName());
			}

			if (rawIdentifier.getDeclaringType().equals(metamodel)) {
				logger.trace("Applying IDENTIFIER " + rawIdentifier.getName() + " type "
						+ rawIdentifier.getType().getTypeName() + " on " + metamodel.getName());
				metamodel.getInFlightAccess().applyIdAttribute((SingularPersistentAttribute<X, ?>) rawIdentifier);
			}
		}

		if (metamodel.hasVersionAttribute()) {
			SingularPersistentAttribute<?, ?> rawVersion = locateVersion(metamodel);

			if (rawVersion.getDeclaringType().equals(metamodel)) {
				metamodel.getInFlightAccess().applyVersionAttribute((SingularPersistentAttribute<X, ?>) rawVersion);
			}
		}
	}

	@SuppressWarnings({ "rawtypes" })
	private <X> Identifier<?, ?> locateIdentifier(ResourceType<X> metamodel) {
		return metamodel == null ? null
				: metamodel.getDeclaredAttributes().stream().filter(attr -> attr instanceof Identifier)
						.map(attr -> (Identifier) attr).findFirst()
						.orElse(locateIdentifier(metamodel.locateSuperType()));
	}

	@SuppressWarnings("rawtypes")
	private <X> Version<?, ?> locateVersion(ResourceType<X> metamodel) {
		return metamodel == null ? null
				: metamodel.getAttributes().stream().filter(attr -> attr instanceof Version).map(attr -> (Version) attr)
						.findFirst().orElse(locateVersion(metamodel.locateSuperType()));
	}

	private <X> void visitInheritance(EntityType<X> metamodel) {
		if (metamodel.getSupertype() == null) {
			logger.trace("Found root " + metamodel.getName());
			return;
		}

		if (!(metamodel instanceof ResourceType)) {
			throw new IllegalArgumentException(
					"Current architecture supports adn.service.resource.metamodel.EntityTypeImpl only :(");
		}

		logger.trace(metamodel.getName() + " extends " + ((EntityType<?>) metamodel.getSupertype()).getName());
	}

	private boolean isProcessingDone(String name) {
		return getSessionFactory().getContextBuildingService().getService(Metadata.class).isProcessingDone(name);
	}

	private Map<String, Class<?>> getImports() {
		return getSessionFactory().getContextBuildingService().getService(Metadata.class).getImports();
	}

	private void markImportAsDone(String name) {
		getSessionFactory().getContextBuildingService().getService(Metadata.class).markImportAsDone(name);
	}

	private class ModelProcessor {

		private final CentralAttributeContext attributeContext;

		private ModelProcessor(CentralAttributeContext attributeContext) {
			// TODO Auto-generated constructor stub
			Assert.notNull(attributeContext, "CentricAttributeContext must not be null");
			this.attributeContext = attributeContext;
		}

		public <J> ResourceType<J> processModel(String name, Class<J> type, Consumer<ResourceType<J>> childCallback)
				throws IllegalAccessException {
			if (isProcessingDone(name)) {
				logger.trace("Ignoring import since metamodel process has already been done: " + type.getName()
						+ ", executing childCallback");

				if (childCallback != null) {
					childCallback.accept(entity(name));
				}

				return entity(name);
			}

			logger.trace(String.format("Processing model. Name: %s. Type: %s", name, type));
			// @formatter:off
			ResourceType<J> metamodel = new ResourceType<>(
					type, name,
					PropertyBinder.INSTANCE.isIdentifierPresented(type),
					PropertyBinder.INSTANCE.isVersionPresented(type),
					ReflectHelper.hasSuperClass(type) ? processModel(
							getImports()
								.entrySet().stream()
								.filter(entry -> entry.getValue().equals(type.getSuperclass()))
								.map(entry -> entry.getKey())
								.findFirst()
								.orElseThrow(() -> new IllegalArgumentException("Unable to obtain supertype of " + type)),
							type.getSuperclass(), (parent) -> parent.getInFlightAccess().addSubclassName(name)) : null,
					isRoot(type));
			// @formatter:on
			processAttributes(metamodel);
			jpaMetamodels.put(name, metamodel);
			importedClassNames.put(type.getName(), name);
			markImportAsDone(name);

			if (childCallback != null) {
				childCallback.accept(metamodel);
			}

			return metamodel;
		}

		private boolean isRoot(Class<?> type) {
			return sessionFactory.getMetadata().getImports().values().stream()
					.filter(imported -> ReflectHelper.isParentOf(type, imported)).count() != 0;
		}

		public <J> void processAttributes(ResourceType<J> metamodel) throws IllegalAccessException {
			Class<J> clazz = metamodel.getJavaType();
			int n = clazz.getDeclaredFields().length;

			for (int i = 0; i < n; i++) {
				final Field f = clazz.getDeclaredFields()[i];
				final AttributeRole role = AttributeRole.getRole(f);
				PersistentAttributeDescriptor<J, ?> attribute = null;

				switch (role) {
					case IDENTIFIER: {
						if (!attributeContext.isBasic(f.getType())) {
							throw new IllegalArgumentException("IDENTIFIER type must be basic type");
						}

						attribute = attributeContext.createIdentifier(metamodel, f);
						break;
					}
					case VERSION: {
						if (!attributeContext.isBasic(f.getType())) {
							throw new IllegalArgumentException("VERSION type must be basic type");
						}

						attribute = attributeContext.createVersion(metamodel, f);
						break;
					}
					case PROPERTY: {
						if (!attributeContext.isPlural(f.getType())) {
							attribute = attributeContext.createSingularAttribute(metamodel, f,
									PropertyBinder.INSTANCE.isOptional(f));
							break;
						}

						attribute = attributeContext.createPluralAttribute(metamodel, f);
						break;
					}
				}

				if (attribute == null) {
					throw new IllegalAccessException("Unable to process attribute " + f.getName());
				}

				logger.trace(String.format("Created [%s] [%s] for type [%s]", attribute.getClass(), attribute.getName(),
						metamodel.getName()));
				metamodel.getInFlightAccess().addAttribute(attribute);
			}
		}

	}

	@SuppressWarnings("serial")
	public static class IdentifierGenerationHolder implements ValueGeneration {

		public static final IdentifierGenerationHolder INSTANCE = new IdentifierGenerationHolder();

		private IdentifierGenerationHolder() {}

		@Override
		public boolean referenceColumnInSql() {
			return false;
		}

		@Override
		public ValueGenerator<?> getValueGenerator() {

			return null;
		}

		@Override
		public GenerationTiming getGenerationTiming() {

			return null;
		}

		@Override
		public String getDatabaseGeneratedReferencedColumnValue() {

			return null;
		}

	}

	@SuppressWarnings("serial")
	public static class NoValueGeneration implements ValueGeneration {

		public static final NoValueGeneration INSTANCE = new NoValueGeneration();

		private NoValueGeneration() {}

		@Override
		public boolean referenceColumnInSql() {
			return false;
		}

		@Override
		public ValueGenerator<?> getValueGenerator() {
			return null;
		}

		@Override
		public GenerationTiming getGenerationTiming() {

			return GenerationTiming.NEVER;
		}

		@Override
		public String getDatabaseGeneratedReferencedColumnValue() {

			return null;
		}

	}

	@Override
	public TypeConfiguration getTypeConfiguration() {

		return sessionFactory.getTypeConfiguration();
	}

	@Override
	public Collection<EntityNameResolver> getEntityNameResolvers() {

		return resourceNameResolvers;
	}

	@Override
	public Map<String, EntityPersister> entityPersisters() {

		return Collections.unmodifiableMap(new HashMap<>(persistersByName));
	}

	@Override
	public CollectionPersister collectionPersister(String role) {

		return null;
	}

	@Override
	public Map<String, CollectionPersister> collectionPersisters() {

		return Collections.emptyMap();
	}

	@Override
	public Set<String> getCollectionRolesByEntityParticipant(String entityName) {

		return Collections.emptySet();
	}

	@Override
	public String[] getAllEntityNames() {
		return sessionFactory.getMetadata().getProcessedImports().toArray(String[]::new);
	}

	@Override
	public String[] getAllCollectionRoles() {

		return new String[0];
	}

	@Override
	public <T> void addNamedEntityGraph(String graphName, RootGraphImplementor<T> entityGraph) {

	}

	@Override
	public <T> RootGraphImplementor<T> findEntityGraphByName(String name) {

		return null;
	}

	@Override
	public <T> List<RootGraphImplementor<? super T>> findEntityGraphsByJavaType(Class<T> entityClass) {

		return null;
	}

	@Override
	public void close() {

	}

	@Override
	public String getImportedClassName(String className) {

		return importedClassNames.get(className);
	}

	@Override
	public String[] getImplementors(String entityName) {

		return new String[0];
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public ResourcePersister<?> entityPersister(Class entityClass) {
		String resourceName = importedClassNames.get(entityClass.getName());

		if (resourceName != null) {
			return persistersByName.get(resourceName);
		}

		return Optional
				.ofNullable(entityPersister(
						resourceNameResolvers.stream().findFirst().orElseThrow().resolveEntityName(entityClass)))
				.orElseThrow(() -> new IllegalArgumentException("Unable to locate persister for type " + entityClass));
	}

	@Override
	public ResourcePersister<?> entityPersister(String entityName) {

		return persistersByName.get(entityName);
	}

	@Override
	public ResourcePersister<?> locateEntityPersister(@SuppressWarnings("rawtypes") Class byClass) {

		return Optional.ofNullable(locateEntityPersister(importedClassNames.get(byClass.getName())))
				.orElseThrow(() -> new IllegalArgumentException("Could not locate ResourcePersister by " + byClass));
	}

	@Override
	public ResourcePersister<?> locateEntityPersister(String byName) {

		return Optional.ofNullable(persistersByName.get(byName)).orElseThrow(
				() -> new IllegalArgumentException("Could not locate ResourcePersister by name " + byName));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ResourcePersister<T> getResourcePersister(Class<T> type) {

		return (ResourcePersister<T>) locateEntityPersister(type);
	}

	@SuppressWarnings("unchecked")
	public <E extends Metamodel> E unwrap(Class<? super E> type) {
		return (E) this;
	}

	@Override
	public EntityManagerFactoryImplementor getSessionFactory() {
		// TODO Auto-generated method stub
		return sessionFactory;
	}

}
