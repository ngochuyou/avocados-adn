/**
 * 
 */
package adn.service.resource.metamodel;

import static adn.helpers.FunctionHelper.reject;
import static adn.service.resource.local.ResourceManagerFactoryBuilder.unsupport;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.PersistenceException;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;

import org.hibernate.mapping.Property;
import org.hibernate.metamodel.model.domain.internal.BasicTypeImpl;
import org.hibernate.metamodel.model.domain.internal.PluralAttributeBuilder;
import org.hibernate.metamodel.model.domain.internal.SingularAttributeImpl;
import org.hibernate.metamodel.model.domain.internal.SingularAttributeImpl.Identifier;
import org.hibernate.metamodel.model.domain.internal.SingularAttributeImpl.Version;
import org.hibernate.metamodel.model.domain.spi.BasicTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.PersistentAttributeDescriptor;
import org.hibernate.metamodel.model.domain.spi.PluralPersistentAttribute;
import org.hibernate.metamodel.model.domain.spi.SingularPersistentAttribute;
import org.hibernate.service.Service;
import org.hibernate.tuple.GenerationTiming;
import org.hibernate.tuple.ValueGeneration;
import org.hibernate.tuple.ValueGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import adn.service.resource.local.ContextBuildingService;
import adn.service.resource.local.Metadata;
import adn.service.resource.local.NamingStrategy;
import adn.service.resource.local.ResourceManagerFactory;
import adn.service.resource.local.ResourcePersister;
import adn.service.resource.local.ResourcePersisterImpl;

/**
 * @author Ngoc Huy
 *
 */
public class MetamodelImpl implements Metamodel {

	private static final Logger logger = LoggerFactory.getLogger(MetamodelImpl.class);

	private final ResourceManagerFactory managerFactory;

	private final Map<String, ResourceType<?>> entitiesByName;
	private final Map<String, ResourcePersister<?>> persistersByName;

	public MetamodelImpl(ContextBuildingService serviceRegistry, ResourceManagerFactory resourceManagerFactory) {
		// TODO Auto-generated constructor stub
		Assert.notNull(resourceManagerFactory, "ResourceManagerFactory must not be null");
		this.managerFactory = resourceManagerFactory;
		this.entitiesByName = new HashMap<>();
		this.persistersByName = new HashMap<>();
	}

	@Override
	public <X> ResourceType<X> entity(Class<X> cls) {
		// TODO Auto-generated method stub
		return entity(managerFactory.getContextBuildingService().getService(NamingStrategy.class).getName(cls));
	}

	@SuppressWarnings("unchecked")
	public <X> ResourceType<X> entity(String name) {
		// TODO Auto-generated method stub
		return (ResourceType<X>) entitiesByName.get(name);
	}

	@Override
	public <X> ResourceType<X> managedType(Class<X> cls) {
		// TODO Auto-generated method stub
		return entity(cls);
	}

	@Override
	public <X> EmbeddableType<X> embeddable(Class<X> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ManagedType<?>> getManagedTypes() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableSet(new HashSet<>(entitiesByName.values()));
	}

	@Override
	public Set<EntityType<?>> getEntities() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableSet(new HashSet<>(entitiesByName.values()));
	}

	@Override
	public Set<EmbeddableType<?>> getEmbeddables() {
		// TODO Auto-generated method stub
		return Collections.emptySet();
	}

	@SuppressWarnings("unchecked")
	public <T> ResourcePersister<T> getResourceDescriptor(String resourceName) {
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
		managerFactory.getContextBuildingService().register(AttributeFactory.class, AttributeFactory.INSTANCE);
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

	private void resolvePersisters() {
		logger.trace("Resolving persisters");

		for (ResourceType<?> metamodel : entitiesByName.values()) {
			ResourcePersister<?> persister;

			persistersByName.put(metamodel.getName(), persister = createPersister(metamodel));
			persister.generateEntityDefinition();
			persister.postInstantiate();
		}
	}

	private void postImport() throws IllegalAccessException {
		entitiesByName.entrySet().stream()
				.map(ele -> ele.getKey() + ": " + (ele.getValue() == null ? "NULL" : ele.getValue().getName()))
				.forEach(logger::trace);
		logger.trace("Post import. Tracing inheritance");

		for (ResourceType<?> metamodel : entitiesByName.values()) {
			visitInheritance(metamodel);
		}

		for (ResourceType<?> metamodel : entitiesByName.values()) {
			logger.trace("Tracing identifier of type: " + metamodel.getName());
			resolveIdentifierAndVersion(metamodel);
		}
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

	private void imports() throws IllegalAccessException {
		ContextBuildingService contextService = managerFactory.getContextBuildingService();
		Metadata metadata = contextService.getService(Metadata.class);
		Map<String, Class<?>> imports = metadata.getImports();
		ModelProcessor modelProcessor = new ModelProcessor();

		for (Map.Entry<String, Class<?>> entry : imports.entrySet()) {
			modelProcessor.processModel(entry.getKey(), entry.getValue());
		}
	}

	private <T> ResourcePersister<T> createPersister(ResourceType<T> metamodel) {
		return new ResourcePersisterImpl<>(managerFactory, metamodel);
	}

	@Override
	public void postProcess() throws PersistenceException {
		// TODO Auto-generated method stub
		ContextBuildingService contextService = managerFactory.getContextBuildingService();
		Metadata metadata = contextService.getService(Metadata.class);

		entitiesByName.values().forEach(metamodel -> {
			logger.trace("Closing access to " + metamodel.getName() + " metamodel");
			((ResourceType<?>) metamodel).getInFlightAccess().finishUp();
		});

		Assert.isTrue(metadata.getImports().keySet().stream()
				.filter(key -> !metadata.isProcessingDone(key) || entity(key) == null).findAny().orElse(null) == null,
				"Processing is not done, cannot invoke postProcess");

		logger.trace("Metamodel building summary:\n"
				+ persistersByName.values().stream().map(ele -> ele.toString()).collect(Collectors.joining("\n")));
	}

	@SuppressWarnings({ "unchecked", "serial" })
	public static class AttributeFactory implements Service {

		static volatile Map<String, BasicTypeDescriptor<?>> TYPE_CONTAINER = new ConcurrentHashMap<>();

		public static AttributeFactory INSTANCE = new AttributeFactory();

		static <D, T> SingularPersistentAttribute<D, T> createIdentifier(ResourceType<D> owner, Field f) {
			// TODO Auto-generated method stub
			return new Identifier<>(owner, f.getName(), INSTANCE.resolveBasicType((Class<T>) f.getType()), f,
					PersistentAttributeType.BASIC);
		}

		static <D, T> SingularPersistentAttribute<D, T> createVersion(ResourceType<D> owner, Field f) {
			return new Version<>(owner, f.getName(), PersistentAttributeType.BASIC,
					INSTANCE.resolveBasicType((Class<T>) f.getType()), f);
		}

		static <D, T> SingularPersistentAttribute<D, T> createSingularAttribute(ResourceType<D> owner, Field f,
				boolean isOptional) {
			return new SingularAttributeImpl<>(owner, f.getName(), PersistentAttributeType.BASIC,
					INSTANCE.resolveBasicType((Class<T>) f.getType()), f, false, false, isOptional);
		}

		static <D, C, E> PluralPersistentAttribute<D, C, E> createPluralAttribute(ResourceType<D> owner, Field f) {
			if (!Collection.class.isAssignableFrom(f.getType())) {
				throw new IllegalArgumentException("PluralAttribute describes Collection property only");
			}

			Class<C> collectionType = (Class<C>) f.getType();
			PluralAttributeBuilder<D, C, E, ?> builder;

			if (collectionType.equals(Map.class)) {
				KeyValueContext<E, ?> kvPair = INSTANCE.determineMapGenericType(f);

				builder = new PluralAttributeBuilder<>(owner, INSTANCE.resolveBasicType(kvPair.keyType), collectionType,
						INSTANCE.resolveBasicType(kvPair.valueType));
			} else {
				builder = new PluralAttributeBuilder<>(owner,
						INSTANCE.resolveBasicType(INSTANCE.determineNonMapGenericType(f)), collectionType, null);
			}

			Property prop = new Property();

			prop.setName(f.getName());
			builder.property(prop);

			return builder.build();
		}

		private <K, V> KeyValueContext<K, V> determineMapGenericType(Field f) {
			if (Map.class.isAssignableFrom(f.getType())) {
				throw new IllegalArgumentException("Unable to extract key, value type out of none-Map collection");
			}

			ParameterizedType paramType = (ParameterizedType) f.getGenericType();

			return new KeyValueContext<>((Class<K>) paramType.getActualTypeArguments()[0],
					(Class<V>) paramType.getActualTypeArguments()[1]);
		}

		private <T> Class<T> determineNonMapGenericType(Field f) {
			ParameterizedType paramType = (ParameterizedType) f.getGenericType();

			return (Class<T>) paramType.getActualTypeArguments()[0];
		}

		private String resolveTypeName(Class<?> type) {
			return type.getName();
		}

		private <T> BasicTypeDescriptor<T> resolveBasicType(Class<T> type) {
			String typeName = resolveTypeName(type);

			if (TYPE_CONTAINER.containsKey(typeName)) {
				BasicTypeDescriptor<?> candidate = TYPE_CONTAINER.get(typeName);

				if (candidate.getJavaType().equals(type)) {
					return (BasicTypeDescriptor<T>) candidate;
				}

				throw new IllegalArgumentException(String.format(
						"Unable to locate BasicTypeDescriptor due to type confliction. Required type %s, found type %s",
						type, candidate.getJavaType()));
			}

			return addType(new BasicTypeImpl<>(type, null));
		}

		static <T> BasicTypeDescriptor<T> addType(BasicTypeDescriptor<T> newType) {
			if (TYPE_CONTAINER.containsKey(INSTANCE.resolveTypeName(newType.getJavaType()))) {
				MetamodelImpl.logger.trace(String.format("Ignoring BasicType contribution: [%s, %s]",
						INSTANCE.resolveTypeName(newType.getJavaType()), newType.getJavaType()));

				return newType;
			}

			TYPE_CONTAINER.put(newType.getTypeName(), newType);
			MetamodelImpl.logger.trace(String.format("New BasicType contribution: [%s, %s]",
					INSTANCE.resolveTypeName(newType.getJavaType()), newType.getJavaType()));

			return newType;
		}

		class KeyValueContext<K, V> {

			Class<K> keyType;

			Class<V> valueType;

			public KeyValueContext(Class<K> keyType, Class<V> valueType) {
				super();
				this.keyType = keyType;
				this.valueType = valueType;
			}

		}

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

		public <J> ResourceType<J> processModel(String name, Class<J> type) throws IllegalAccessException {
			if (isProcessingDone(name)) {
				logger.trace("Ignoring import since metamodel process has already been done: " + type.getName());
				return entity(type);
			}

			logger.trace(String.format("Processing model. Name: %s. Type: %s", name, type));
			// @formatter:off
			ResourceType<J> metamodel = new ResourceType<>(
					type, name,
					isIdentifierPresented(type),
					isVersionPresented(type),
					type.getSuperclass() != null && type.getSuperclass() != Object.class ? processModel(
							getImports()
								.entrySet().stream()
								.filter(entry -> entry.getValue().equals(type.getSuperclass()))
								.map(entry -> entry.getKey())
								.findFirst()
								.orElseThrow(() -> new IllegalArgumentException("Unable to obtain supertype of " + type)),
							type.getSuperclass()) : null);
			// @formatter:on
			processAttributes(metamodel);
			entitiesByName.put(name, metamodel);
			markImportAsDone(name);

			return metamodel;
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
						logger.trace("Creating IDENTIFIER for type: " + metamodel.getJavaType());
						attribute = AttributeFactory.createIdentifier(metamodel, f);
						break;
					}
					case VERSION: {
						logger.trace("Creating VERSION for type: " + metamodel.getJavaType());
						attribute = AttributeFactory.createVersion(metamodel, f);
						break;
					}
					case PROPERTY: {
						if (!isPlural(f)) {
							logger.trace("Creating SingularAttribute for type: " + metamodel.getJavaType());
							attribute = AttributeFactory.createSingularAttribute(metamodel, f, isAttributeOptional(f));
							break;
						}

						logger.trace("Creating PluralAttribute for type: " + metamodel.getJavaType());
						attribute = AttributeFactory.createPluralAttribute(metamodel, f);
						break;
					}
				}

				if (attribute == null) {
					throw new IllegalAccessException("Unable to process attribute " + f.getName());
				}

				metamodel.getInFlightAccess().addAttribute(attribute);
			}
		}

		private boolean isAttributeOptional(Field f) {
			Column colAnno = f.getDeclaredAnnotation(Column.class);

			if (colAnno == null) {
				return false;
			}

			return colAnno.nullable();
		}

		private boolean isIdentifierPresented(Class<?> clazz) throws SecurityException, IllegalAccessException {
			// @formatter:off
			long n = 0;
			
			return (n = Stream.of(clazz.getDeclaredFields())
					.map(field -> field.getDeclaredAnnotation(Id.class) != null)
					.filter(pred -> pred)
					.count()) < 2 ? n == 1 : reject("More than one @Id were found in type: " + clazz);
			// @formatter:on
		}

		private boolean isVersionPresented(Class<?> clazz) throws SecurityException, IllegalAccessException {
			// @formatter:off
			long n = 0;
			
			return (n = Stream.of(clazz.getDeclaredFields())
					.map(field -> field.getDeclaredAnnotation(javax.persistence.Version.class) != null)
					.filter(pred -> pred)
					.count()) < 2 ? n == 1 : reject("More than one @Version were found in type: " + clazz);
			// @formatter:on
		}

		private boolean isPlural(Field f) {
			return Collection.class.isAssignableFrom(f.getType());
		}

	}

	private static enum AttributeRole {

		IDENTIFIER, VERSION, PROPERTY;

		static AttributeRole getRole(Field f) {
			Id idAnno = f.getDeclaredAnnotation(Id.class);
			javax.persistence.Version versionAnno = f.getDeclaredAnnotation(javax.persistence.Version.class);

			if (idAnno != null && versionAnno != null) {
				throw new IllegalArgumentException(
						"@Id and @Version collision on " + f.getDeclaringClass() + "." + f.getName());
			}

			return idAnno != null ? IDENTIFIER : versionAnno != null ? VERSION : PROPERTY;
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

}
