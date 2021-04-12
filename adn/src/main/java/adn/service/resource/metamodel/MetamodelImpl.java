/**
 * 
 */
package adn.service.resource.metamodel;

import static adn.helpers.FunctionHelper.reject;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceException;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GeneratorType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.spi.MutableIdentifierGeneratorFactory;
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
import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.GetterMethodImpl;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.property.access.spi.SetterMethodImpl;
import org.hibernate.service.Service;
import org.hibernate.tuple.CreationTimestampGeneration;
import org.hibernate.tuple.UpdateTimestampGeneration;
import org.hibernate.tuple.ValueGeneration;
import org.hibernate.tuple.VmValueGeneration;
import org.hibernate.type.BasicTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import adn.helpers.StringHelper;
import adn.service.resource.local.ContextBuildingService;
import adn.service.resource.local.Metadata;
import adn.service.resource.local.NamingStrategy;
import adn.service.resource.local.ResourceDescriptor;
import adn.service.resource.local.ResourceManagerFactory;

/**
 * @author Ngoc Huy
 *
 */
public class MetamodelImpl implements Metamodel {

	private static final Logger logger = LoggerFactory.getLogger(MetamodelImpl.class);

	private final ResourceManagerFactory managerFactory;

	private final Map<String, EntityTypeImpl<?>> entitiesByName;
	private final Map<String, ResourceDescriptor<?>> descriptorsByName;

	public MetamodelImpl(ContextBuildingService serviceRegistry, ResourceManagerFactory resourceManagerFactory) {
		// TODO Auto-generated constructor stub
		Assert.notNull(resourceManagerFactory, "ResourceManagerFactory must not be null");
		this.managerFactory = resourceManagerFactory;
		this.entitiesByName = new HashMap<>();
		this.descriptorsByName = new HashMap<>();
	}

	@Override
	public <X> EntityTypeImpl<X> entity(Class<X> cls) {
		// TODO Auto-generated method stub
		return entity(managerFactory.getContextBuildingService().getService(NamingStrategy.class).getName(cls));
	}

	@SuppressWarnings("unchecked")
	public <X> EntityTypeImpl<X> entity(String name) {
		// TODO Auto-generated method stub
		return (EntityTypeImpl<X>) entitiesByName.get(name);
	}

	@Override
	public <X> EntityTypeImpl<X> managedType(Class<X> cls) {
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
	public <T> ResourceDescriptor<T> getResourceDescriptor(String resourceName) {
		// TODO Auto-generated method stub
		if (!descriptorsByName.keySet().contains(resourceName)) {
			return null;
		}

		return (ResourceDescriptor<T>) descriptorsByName.get(resourceName);
	}

	public Set<ResourceDescriptor<?>> getResourceDescriptors() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableSet(new HashSet<>(descriptorsByName.values()));
	}

	public ResourceManagerFactory getManagerFactory() {
		return managerFactory;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		for (ResourceDescriptor<?> descriptor : descriptorsByName.values()) {
			// @formatter:off
			logger.debug(String.format("\nCreated descriptor for type: %s with name: %s\n"
					+ "\t-idGetter: %s returns %s\n"
					+ "\t-idSetter: %s returns void\n"
					+ "\t-isIdentifierAutoGenerated: %s\n"
					+ "\t-identifierValueGenerator: %s\n"
					+ "\t-generationTiming: %s",
					descriptor.getType(), descriptor.getResourceName(),
					descriptor.getIdentifierGetter().getMethodName(), descriptor.getIdentifierGetter().getReturnType(),
					descriptor.getIdentifierSetter().getMethodName(),
					Boolean.valueOf(descriptor.isIdentifierAutoGenerated()),
					(descriptor.isIdentifierAutoGenerated() ? 
							descriptor.getIdentifierValueGeneration().getValueGenerator().getClass().getName() :
							"NONE"),
					(descriptor.isIdentifierAutoGenerated() ? 
							descriptor.getIdentifierValueGeneration().getGenerationTiming() :
							"NEVER")));
			logger.trace(descriptor.toString());
		}
		// @formatter:on
		return super.toString();
	}

	@Override
	public void prepare() throws PersistenceException {
		// TODO Auto-generated method stub
		managerFactory.getContextBuildingService().register(AttributeFactory.class, AttributeFactory.INSTANCE);
		managerFactory.getContextBuildingService().register(IdentifierGeneratorFactory.class,
				new IdentifierGeneratorFactory(managerFactory.getContextBuildingService()));
	}

	@Override
	public void process() throws PersistenceException {
		// TODO Auto-generated method stub
		try {
			imports();
			postImport();
		} catch (Exception e) {
			e.printStackTrace();
			throw new PersistenceException(e);
		}
	}

	private void postImport() throws IllegalAccessException {
		entitiesByName.entrySet().stream()
				.map(ele -> ele.getKey() + ": " + (ele.getValue() == null ? "NULL" : ele.getValue().toString()))
				.forEach(logger::trace);
		logger.trace("Post import. Tracing inheritance");

		for (EntityTypeImpl<?> metamodel : entitiesByName.values()) {
			visitInheritance(metamodel);
		}

		for (EntityTypeImpl<?> metamodel : entitiesByName.values()) {
			logger.trace("Tracing identifier of type: " + metamodel.getName());
			processIdentifierAndVersion(metamodel);
		}
	}

	@SuppressWarnings("unchecked")
	private <X> void processIdentifierAndVersion(EntityTypeImpl<X> metamodel) throws IllegalAccessException {
		if (metamodel.hasSingleIdAttribute()) {
			SingularPersistentAttribute<?, ?> rawIdentifier = locateIdentifier(metamodel);

			if (rawIdentifier == null) {
				throw new IllegalArgumentException(
						"Unable to locate Identifier of type: " + metamodel.getJavaType().getName());
			}

			if (rawIdentifier.getDeclaringType().equals(metamodel)) {
				metamodel.getInFlightAccess().applyIdAttribute((SingularPersistentAttribute<X, ?>) rawIdentifier);

				if (metamodel.isPropertyAutoGenerated(rawIdentifier.getName())) {
					metamodel.getInflightAccess().setIdentifierGenerator(locateIdentifierGenerator(metamodel));
				}
			}

			String idName = metamodel.getId(metamodel.getIdType().getJavaType()).getName();

			metamodel.getInflightAccess().setIdGetter(metamodel.getGetter(idName));
			metamodel.getInflightAccess().setIdSetter(metamodel.getSetter(idName));
		}

		if (metamodel.hasVersionAttribute()) {
			SingularPersistentAttribute<?, ?> rawVersion = locateVersion(metamodel);

			if (rawVersion.getDeclaringType().equals(metamodel)) {
				metamodel.getInFlightAccess().applyVersionAttribute((SingularPersistentAttribute<X, ?>) rawVersion);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <X, T> IdentifierGenerator locateIdentifierGenerator(EntityTypeImpl<X> metamodel)
			throws IllegalAccessException {
		return locateIdentifierGenerator(metamodel, (Class<T>) metamodel.getIdType().getJavaType());
	}

	private <X, T> IdentifierGenerator locateIdentifierGenerator(EntityTypeImpl<X> metamodel, Class<T> identifierType)
			throws IllegalAccessException {
		Assert.notNull(identifierType, "Identifier type must not be null");

		SingularPersistentAttribute<X, T> idAttribute = metamodel.getDeclaredId(identifierType);
		Field idField = ((Field) idAttribute.getJavaMember());
		GenericGenerator ggAnno = idField.getDeclaredAnnotation(GenericGenerator.class);

		Assert.notNull(ggAnno,
				"Unable to locate @GenericGenerator field " + metamodel.getJavaType() + "." + idField.getName());

		String generatorName = ggAnno.strategy();

		Assert.hasLength(generatorName, "Invalid IdentifierGenrator name");

		ContextBuildingService contextService = managerFactory.getContextBuildingService();
		IdentifierGeneratorFactory igFactory = contextService.getService(IdentifierGeneratorFactory.class);
		IdentifierGenerator generator = igFactory.obtainGenerator(generatorName);

		generator = generator != null ? generator : igFactory.register(generatorName, idAttribute.getJavaType());

		Assert.notNull(generator, "Unable to locate IdentifierGenrator for " + idAttribute.getName());

		return generator;
	}

	@SuppressWarnings("serial")
	private static class IdentifierGeneratorFactory implements Service {

		private final Map<String, IdentifierGenerator> generatorMap = new HashMap<>();

		private final ContextBuildingService contextService;
		private final BasicTypeRegistry typeRegistry;

		private IdentifierGeneratorFactory(ContextBuildingService contextService) {
			this.contextService = contextService;
			typeRegistry = contextService.getServiceWrapper(BasicTypeRegistry.class,
					wrapper -> wrapper.unwrap().getClass().equals(BasicTypeRegistry.class),
					wrapper -> wrapper.orElseThrow().unwrap());
		}

		public <X extends IdentifierGenerator> IdentifierGenerator obtainGenerator(String name) {
			if (!generatorMap.containsKey(name)) {
				return null;
			}

			return generatorMap.get(name);
		}

		public <X extends IdentifierGenerator> IdentifierGenerator register(String name, Class<?> identifierJavaType) {
			Assert.notNull(name, "IdentifierGenerator name must not be null");
			Assert.notNull(identifierJavaType, "Identifier type must not be null");

			if (generatorMap.containsKey(name)) {
				logger.trace("Ignoring registration of IdentifierGenerator of name " + name);
				return generatorMap.get(name);
			}

			logger.trace("Creating new IdentifierGenerator of name " + name);

			generatorMap.put(name,
					contextService.getService(MutableIdentifierGeneratorFactory.class).createIdentifierGenerator(name,
							typeRegistry.getRegisteredType(identifierJavaType.getName()), null));

			return generatorMap.get(name);
		}

	}

	@SuppressWarnings({ "rawtypes" })
	private <X> Identifier<?, ?> locateIdentifier(EntityTypeImpl<X> metamodel) {
		return metamodel == null ? null
				: metamodel.getAttributes().stream().filter(attr -> attr instanceof Identifier)
						.map(attr -> (Identifier) attr).findFirst()
						.orElse(locateIdentifier((EntityTypeImpl<? super X>) metamodel.getSupertype()));
	}

	@SuppressWarnings("rawtypes")
	private <X> Version<?, ?> locateVersion(EntityTypeImpl<X> metamodel) {
		return metamodel == null ? null
				: metamodel.getAttributes().stream().filter(attr -> attr instanceof Version).map(attr -> (Version) attr)
						.findFirst().orElse(locateVersion((EntityTypeImpl<? super X>) metamodel.getSupertype()));
	}

	private <X> void visitInheritance(EntityType<X> metamodel) {
		if (metamodel.getSupertype() == null) {
			logger.trace("Found root " + metamodel.getName());
			return;
		}

		if (!(metamodel instanceof EntityTypeImpl)) {
			throw new IllegalArgumentException(
					"Current architecture supports adn.service.resource.metamodel.EntityTypeImpl only :(");
		}

		logger.trace(metamodel.getName() + " extends " + ((EntityType<?>) metamodel.getSupertype()).getName());
	}

	private void imports() throws IllegalAccessException {
		ContextBuildingService contextService = managerFactory.getContextBuildingService();
		Metadata metadata = contextService.getService(Metadata.class);
		Map<String, Class<?>> imports = metadata.getImports();
		ModelProcessor processor = new ModelProcessor();

		logger.trace("Importing models");

		for (String name : imports.keySet()) {
			processor.processModel(name, imports.get(name));
		}
	}

	@Override
	public void postProcess() throws PersistenceException {
		// TODO Auto-generated method stub
		ContextBuildingService contextService = managerFactory.getContextBuildingService();
		Metadata metadata = contextService.getService(Metadata.class);

		entitiesByName.values().forEach(metamodel -> {
			logger.trace("Closing access to " + metamodel.getName() + " metamodel");
			((EntityTypeImpl<?>) metamodel).closeAccess();
		});

		Assert.isTrue(metadata.getImports().keySet().stream()
				.filter(key -> !metadata.isProcessingDone(key) || entity(key) == null).findAny().orElse(null) == null,
				"Processing is not done, cannot invoke postProcess");

		logger.trace("Metamodel building summary:\n"
				+ entitiesByName.values().stream().map(ele -> ele.log()).collect(Collectors.joining("\n")));
	}

	@SuppressWarnings({ "unchecked", "serial" })
	public static class AttributeFactory implements Service {

		static volatile Map<String, BasicTypeDescriptor<?>> TYPE_CONTAINER = new ConcurrentHashMap<>();

		public static AttributeFactory INSTANCE = new AttributeFactory();

		static <D, T> SingularPersistentAttribute<D, T> createIdentifier(EntityTypeImpl<D> owner, Field f) {
			// TODO Auto-generated method stub
			return new Identifier<>(owner, f.getName(), INSTANCE.resolveBasicType((Class<T>) f.getType()), f,
					PersistentAttributeType.BASIC);
		}

		static <D, T> SingularPersistentAttribute<D, T> createVersion(EntityTypeImpl<D> owner, Field f) {
			return new Version<>(owner, f.getName(), PersistentAttributeType.BASIC,
					INSTANCE.resolveBasicType((Class<T>) f.getType()), f);
		}

		static <D, T> SingularPersistentAttribute<D, T> createSingularAttribute(EntityTypeImpl<D> owner, Field f,
				boolean isOptional) {
			return new SingularAttributeImpl<>(owner, f.getName(), PersistentAttributeType.BASIC,
					INSTANCE.resolveBasicType((Class<T>) f.getType()), f, false, false, isOptional);
		}

		static <D, C, E> PluralPersistentAttribute<D, C, E> createPluralAttribute(EntityTypeImpl<D> owner, Field f) {
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

		public <J> EntityTypeImpl<J> processModel(String name, Class<J> type) throws IllegalAccessException {
			if (isProcessingDone(name)) {
				logger.trace("Ignoring import since metamodel process has already been done: " + type.getName());
				return entity(type);
			}

			logger.trace(String.format("Processing model. Name: %s. Type: %s", name, type));
			// @formatter:off
			EntityTypeImpl<J> metamodel = new EntityTypeImpl<>(
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
			processMetadata(metamodel);
			entitiesByName.put(name, metamodel);
			markImportAsDone(name);

			return metamodel;
		}

		public <J> void processAttributes(EntityTypeImpl<J> metamodel) throws IllegalAccessException {
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
				metamodel.getInflightAccess().putIndex(i, f.getName());
				metamodel.getInflightAccess().increasePropertySpan();
			}
		}

		@SuppressWarnings("unchecked")
		public <J> void processMetadata(EntityTypeImpl<J> metamodel) throws IllegalAccessException {
			Attribute<? super J, ?>[] attributes = metamodel.getDeclaredAttributes().toArray(Attribute[]::new);
			int n = attributes.length;
			Getter[] getters = new Getter[n];
			Class<?>[] types = new Class<?>[n];

			for (int i = 0; i < n; i++) {
				getters[metamodel.getPropertyIndex(attributes[i].getName())] = resolveGetter(metamodel, attributes[i]);
				types[metamodel.getPropertyIndex(attributes[i].getName())] = attributes[i].getJavaType();
			}

			metamodel.getInflightAccess().setGetters(getters);
			metamodel.getInflightAccess().setPropertyTypes(types);

			Setter[] setters = new Setter[n];

			for (int i = 0; i < n; i++) {
				setters[metamodel.getPropertyIndex(attributes[i].getName())] = resolveSetter(metamodel, attributes[i]);
			}

			metamodel.getInflightAccess().setSetters(setters);

			ValueGeneration[] vgs = new ValueGeneration[n];
			boolean[] autoGeneratedMarkers = new boolean[n];

			for (int i = 0; i < n; i++) {
				ValueGeneration delegateGeneration;

				vgs[metamodel.getPropertyIndex(attributes[i]
						.getName())] = (delegateGeneration = resolveValueGeneration(metamodel, attributes[i]));
				logger.trace(String.format(
						"Located valuegeneration of attribute %s. GenerationTiming %s. ValueGenerator %s",
						attributes[i].getName(),
						delegateGeneration == IdentifierGenerationHolder.INSTANCE ? "determined by IdentifierGenerator"
								: delegateGeneration.getGenerationTiming(),
						delegateGeneration.getValueGenerator() != null
								? delegateGeneration.getValueGenerator().getClass().getName()
								: delegateGeneration == IdentifierGenerationHolder.INSTANCE
										? "generated by IdentifierGenerator"
										: "NULL"));
				autoGeneratedMarkers[metamodel.getPropertyIndex(
						attributes[i].getName())] = (delegateGeneration != NoValueGeneration.INSTANCE);
			}

			metamodel.getInflightAccess().setValueGenerations(vgs);
			metamodel.getInflightAccess().setAutoGeneratedMarkers(autoGeneratedMarkers);
		}

		private <X> ValueGeneration resolveValueGeneration(EntityTypeImpl<X> metamodel, Attribute<?, ?> attribute) {
			Field f = (Field) attribute.getJavaMember();

			if (attribute instanceof Identifier && f.getDeclaredAnnotation(GeneratedValue.class) != null) {
				return IdentifierGenerationHolder.INSTANCE;
			}

			if (f.getDeclaredAnnotation(CreationTimestamp.class) != null) {
				if (!doesSupport(f.getType())) {
					throw new IllegalArgumentException(
							"Unable to generate non-java.util.Date for CreationTimestamp attributes");
				}

				CreationTimestampGeneration generation = new CreationTimestampGeneration();

				generation.initialize(f.getDeclaredAnnotation(CreationTimestamp.class), Date.class);

				return generation;
			}

			if (f.getDeclaredAnnotation(UpdateTimestamp.class) != null) {
				if (!doesSupport(f.getType())) {
					throw new IllegalArgumentException(
							"Unable to generate non-java.util.Date for UpdateTimestamp attributes");
				}

				UpdateTimestampGeneration generation = new UpdateTimestampGeneration();

				generation.initialize(f.getDeclaredAnnotation(UpdateTimestamp.class), Date.class);

				return generation;
			}

			if (f.getDeclaredAnnotation(GeneratedValue.class) != null) {
				GeneratorType gta;

				if ((gta = f.getDeclaredAnnotation(GeneratorType.class)) == null) {
					throw new IllegalArgumentException("GeneratorType required on GeneratedValue attributes");
				}

				VmValueGeneration generation = new VmValueGeneration();

				generation.initialize(gta, f.getType());

				return generation;
			}

			return NoValueGeneration.INSTANCE;
		}

		private boolean doesSupport(Class<?> type) {
			for (Class<?> supported : new Class<?>[] { Date.class, Calendar.class, java.sql.Date.class, Time.class,
					Timestamp.class, Instant.class, LocalDate.class, LocalDateTime.class, LocalTime.class,
					MonthDay.class, OffsetDateTime.class, OffsetTime.class, Year.class, YearMonth.class,
					ZonedDateTime.class }) {
				if (supported == type) {
					return true;
				}
			}

			return false;
		}

		private <X> Getter resolveGetter(EntityTypeImpl<X> metamodel, Attribute<?, ?> attr) {
			Getter getter;
			String getterName = StringHelper.toCamel("get " + attr.getJavaMember().getName(),
					StringHelper.MULTIPLE_MATCHES_WHITESPACE_CHARS);

			try {
				getter = new GetterMethodImpl(metamodel.getJavaType(), attr.getName(),
						metamodel.getJavaType().getDeclaredMethod(getterName));
				logger.trace(
						String.format("Created %s.%s()", getter.getReturnType().getName(), getter.getMethodName()));

				return getter;
			} catch (NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				logger.error("Unable to locate " + getterName + " of type: " + metamodel.getJavaType().getName());

				return null;
			}
		}

		private <X> Setter resolveSetter(EntityTypeImpl<X> metamodel, Attribute<?, ?> attr) {
			Setter setter;
			String setterName = StringHelper.toCamel("set " + attr.getJavaMember().getName(),
					StringHelper.MULTIPLE_MATCHES_WHITESPACE_CHARS);

			try {
				setter = new SetterMethodImpl(metamodel.getJavaType(), attr.getName(),
						metamodel.getJavaType().getDeclaredMethod(setterName, attr.getJavaType()));
				logger.trace(String.format("Created %s(%s)", setter.getMethodName(), attr.getJavaType().getName()));

				return setter;
			} catch (NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				logger.error("Unable to locate " + setterName + " of type: " + metamodel.getJavaType().getName());

				return null;
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

}
