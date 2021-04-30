/**
 * 
 */
package adn.service.resource.metamodel;

import static adn.service.resource.local.ResourceManagerFactoryBuilder.unsupport;

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
import org.hibernate.type.BasicTypeRegistry;
import org.hibernate.type.spi.TypeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import adn.helpers.ReflectHelper;
import adn.service.resource.local.ContextBuildingService;
import adn.service.resource.local.Metadata;
import adn.service.resource.local.NamingStrategy;
import adn.service.resource.local.ResourceManagerFactory;
import adn.service.resource.local.ResourcePersister;
import adn.service.resource.local.ResourcePersisterImpl;
import adn.service.resource.metamodel.PropertyBinder.AttributeRole;

/**
 * @author Ngoc Huy
 *
 */
public class MetamodelImpl implements Metamodel, MetamodelImplementor {

	private static final Logger logger = LoggerFactory.getLogger(MetamodelImpl.class);

	private final ResourceManagerFactory managerFactory;

	private final Map<String, String> importedClassNames;
	private final Map<String, ResourceType<?>> resourceTypesByName;
	private final Map<String, ResourcePersister<?>> persistersByName;

	private final Set<EntityNameResolver> resourceNameResolvers;

	public MetamodelImpl(ContextBuildingService serviceRegistry, ResourceManagerFactory resourceManagerFactory) {
		// TODO Auto-generated constructor stub
		Assert.notNull(resourceManagerFactory, "ResourceManagerFactory must not be null");
		this.managerFactory = resourceManagerFactory;
		this.resourceTypesByName = new HashMap<>();
		this.persistersByName = new HashMap<>();
		this.resourceNameResolvers = new HashSet<>();
		this.importedClassNames = new HashMap<>();
	}

	@Override
	public <X> ResourceType<X> entity(Class<X> cls) {
		// TODO Auto-generated method stub
		return entity(managerFactory.getContextBuildingService().getService(NamingStrategy.class).getName(cls));
	}

	@SuppressWarnings("unchecked")
	public <X> ResourceType<X> entity(String name) {
		// TODO Auto-generated method stub
		if (!resourceTypesByName.containsKey(name)) {
			return null;
		}

		return (ResourceType<X>) resourceTypesByName.get(name);
	}

	@Override
	public <X> ResourceType<X> managedType(Class<X> cls) {
		// TODO Auto-generated method stub
		return entity(cls);
	}

	@Override
	public <X> EmbeddedTypeDescriptor<X> embeddable(Class<X> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ManagedType<?>> getManagedTypes() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableSet(new HashSet<>(resourceTypesByName.values()));
	}

	@Override
	public Set<EntityType<?>> getEntities() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableSet(new HashSet<>(resourceTypesByName.values()));
	}

	@Override
	public Set<EmbeddableType<?>> getEmbeddables() {
		// TODO Auto-generated method stub
		return Collections.emptySet();
	}

	@SuppressWarnings("unchecked")
	public <T> ResourcePersister<T> getResourcePersister(String resourceName) {
		// TODO Auto-generated method stub
		if (!persistersByName.keySet().contains(resourceName)) {
			return null;
		}

		return (ResourcePersister<T>) persistersByName.get(resourceName);
	}

	public Set<ResourcePersister<?>> getResourceDescriptors() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableSet(new HashSet<>(persistersByName.values()));
	}

	public ResourceManagerFactory getManagerFactory() {
		return managerFactory;
	}

	@Override
	public void prepare() throws PersistenceException {
		// TODO Auto-generated method stub
		BasicTypeRegistry typeRegistry = managerFactory.getContextBuildingService()
				.getServiceWrapper(BasicTypeRegistry.class, wrapper -> wrapper.orElseThrow().unwrap());

		Assert.notNull(typeRegistry, "BasicTypeRegistry must not be null");
		managerFactory.getContextBuildingService().register(CentralAttributeContext.class,
				new CentralAttributeContext.CentralAttributeContextImpl(typeRegistry, this));
		resourceNameResolvers.add(managerFactory.getContextBuildingService().getService(NamingStrategy.class));
	}

	@Override
	public void process() throws PersistenceException {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		ContextBuildingService contextService = managerFactory.getContextBuildingService();
		Metadata metadata = contextService.getService(Metadata.class);

		resourceTypesByName.values().forEach(metamodel -> {
			logger.trace("Closing access to " + metamodel.getName() + " metamodel");
			((ResourceType<?>) metamodel).getInFlightAccess().finishUp();
		});

		Assert.isTrue(metadata.getImports().keySet().stream()
				.filter(key -> !metadata.isProcessingDone(key) || entity(key) == null).findAny().orElse(null) == null,
				"Processing is not done, cannot invoke postProcess");

		logger.trace("Metamodel building summary:\n"
				+ persistersByName.values().stream().map(ele -> ele.toString()).collect(Collectors.joining("\n")));
	}

	private void imports() throws IllegalAccessException {
		ContextBuildingService contextService = managerFactory.getContextBuildingService();
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

		for (ResourceType<?> metamodel : resourceTypesByName.values()) {
			visitInheritance(metamodel);
		}

		for (ResourceType<?> metamodel : resourceTypesByName.values()) {
			logger.trace("Tracing identifier of type: " + metamodel.getName());
			resolveIdentifierAndVersion(metamodel);
		}
	}

	private void resolvePersisters() {
		logger.trace("Resolving persisters");

		for (ResourceType<?> metamodel : resourceTypesByName.values()) {
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
		return new ResourcePersisterImpl<>(managerFactory, metamodel);
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
		return getManagerFactory().getContextBuildingService().getService(Metadata.class).isProcessingDone(name);
	}

	private Map<String, Class<?>> getImports() {
		return getManagerFactory().getContextBuildingService().getService(Metadata.class).getImports();
	}

	private void markImportAsDone(String name) {
		getManagerFactory().getContextBuildingService().getService(Metadata.class).markImportAsDone(name);
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
			resourceTypesByName.put(name, metamodel);
			importedClassNames.put(type.getName(), name);
			markImportAsDone(name);

			if (childCallback != null) {
				childCallback.accept(metamodel);
			}

			return metamodel;
		}

		private boolean isRoot(Class<?> type) {
			return managerFactory.getMetadata().getImports().values().stream()
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

						logger.trace("Creating IDENTIFIER for type: " + metamodel.getJavaType());
						attribute = attributeContext.createIdentifier(metamodel, f);
						break;
					}
					case VERSION: {
						if (!attributeContext.isBasic(f.getType())) {
							throw new IllegalArgumentException("VERSION type must be basic type");
						}

						logger.trace("Creating VERSION for type: " + metamodel.getJavaType());
						attribute = attributeContext.createVersion(metamodel, f);
						break;
					}
					case PROPERTY: {
						if (!attributeContext.isPlural(f.getType())) {
							logger.trace("Creating SingularAttribute for type: " + metamodel.getJavaType());
							attribute = attributeContext.createSingularAttribute(metamodel, f,
									PropertyBinder.INSTANCE.isOptional(f));
							break;
						}

						logger.trace("Creating PluralAttribute for type: " + metamodel.getJavaType());
						attribute = attributeContext.createPluralAttribute(metamodel, f);
						break;
					}
				}

				if (attribute == null) {
					throw new IllegalAccessException("Unable to process attribute " + f.getName());
				}

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
			unsupport();
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public ValueGenerator<?> getValueGenerator() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GenerationTiming getGenerationTiming() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getDatabaseGeneratedReferencedColumnValue() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	@SuppressWarnings("serial")
	public static class NoValueGeneration implements ValueGeneration {

		public static final NoValueGeneration INSTANCE = new NoValueGeneration();

		private NoValueGeneration() {}

		@Override
		public boolean referenceColumnInSql() {
			// TODO Auto-generated method stub
			unsupport();
			return false;
		}

		@Override
		public ValueGenerator<?> getValueGenerator() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public GenerationTiming getGenerationTiming() {
			// TODO Auto-generated method stub
			return GenerationTiming.NEVER;
		}

		@Override
		public String getDatabaseGeneratedReferencedColumnValue() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	@Override
	public TypeConfiguration getTypeConfiguration() {
		// TODO Auto-generated method stub
		return managerFactory.getTypeConfiguration();
	}

	@Override
	public Collection<EntityNameResolver> getEntityNameResolvers() {
		// TODO Auto-generated method stub
		return resourceNameResolvers;
	}

	@Override
	public Map<String, EntityPersister> entityPersisters() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableMap(new HashMap<>(persistersByName));
	}

	@Override
	public CollectionPersister collectionPersister(String role) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, CollectionPersister> collectionPersisters() {
		// TODO Auto-generated method stub
		return Collections.emptyMap();
	}

	@Override
	public Set<String> getCollectionRolesByEntityParticipant(String entityName) {
		// TODO Auto-generated method stub
		return Collections.emptySet();
	}

	@Override
	public String[] getAllEntityNames() {
		// TODO Auto-generated method stub
		return managerFactory.getMetadata().getProcessedImports().toArray(String[]::new);
	}

	@Override
	public String[] getAllCollectionRoles() {
		// TODO Auto-generated method stub
		return new String[0];
	}

	@Override
	public <T> void addNamedEntityGraph(String graphName, RootGraphImplementor<T> entityGraph) {
		// TODO Auto-generated method stub
	}

	@Override
	public <T> RootGraphImplementor<T> findEntityGraphByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<RootGraphImplementor<? super T>> findEntityGraphsByJavaType(Class<T> entityClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
	}

	@Override
	public String getImportedClassName(String className) {
		// TODO Auto-generated method stub
		return importedClassNames.get(className);
	}

	@Override
	public String[] getImplementors(String entityName) {
		// TODO Auto-generated method stub
		return new String[0];
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unlikely-arg-type" })
	public ResourcePersister<?> entityPersister(Class entityClass) {
		// TODO Auto-generated method stub
		String resourceName = importedClassNames.get(entityClass);

		if (resourceName != null) {
			return persistersByName.get(resourceName);
		}

		Assert.isTrue(!resourceNameResolvers.isEmpty(), "Unable to locate name resolver");
		resourceName = resourceNameResolvers.stream().findFirst().orElseThrow().resolveEntityName(entityClass);

		return Optional.ofNullable(entityPersister(resourceName)).orElse(null);
	}

	@Override
	public ResourcePersister<?> entityPersister(String entityName) {
		// TODO Auto-generated method stub
		return persistersByName.get(entityName);
	}

	@Override
	public ResourcePersister<?> locateEntityPersister(@SuppressWarnings("rawtypes") Class byClass) {
		// TODO Auto-generated method stub
		return Optional.ofNullable(locateEntityPersister(importedClassNames.get(byClass.getName())))
				.orElseThrow(() -> new IllegalArgumentException("Could not locate ResourcePersister by " + byClass));
	}

	@Override
	public ResourcePersister<?> locateEntityPersister(String byName) {
		// TODO Auto-generated method stub
		return Optional.ofNullable(persistersByName.get(byName)).orElseThrow(
				() -> new IllegalArgumentException("Could not locate ResourcePersister by name " + byName));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> ResourcePersister<T> getResourcePersister(Class<T> type) {
		// TODO Auto-generated method stub
		return (ResourcePersister<T>) locateEntityPersister(type);
	}

	@SuppressWarnings("unchecked")
	public <E extends Metamodel> E unwrap(Class<? super E> type) {
		return (E) this;
	}

}
