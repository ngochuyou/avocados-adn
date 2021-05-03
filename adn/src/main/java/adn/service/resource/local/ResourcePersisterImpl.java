package adn.service.resource.local;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.el.PropertyNotFoundException;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;

import org.hibernate.HibernateException;
import org.hibernate.InstantiationException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.bytecode.spi.BytecodeEnhancementMetadata;
import org.hibernate.dialect.lock.LockingStrategy;
import org.hibernate.engine.internal.MutableEntityEntryFactory;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.EntityEntryFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.engine.spi.ValueInclusion;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.internal.FilterAliasGenerator;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.loader.entity.UniqueEntityLoader;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metamodel.model.domain.spi.SingularPersistentAttribute;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.Lockable;
import org.hibernate.persister.entity.MultiLoadOptions;
import org.hibernate.persister.walking.spi.AttributeDefinition;
import org.hibernate.persister.walking.spi.EntityIdentifierDefinition;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.tuple.GenerationTiming;
import org.hibernate.tuple.Instantiator;
import org.hibernate.tuple.PojoInstantiator;
import org.hibernate.tuple.ValueGeneration;
import org.hibernate.tuple.entity.BytecodeEnhancementMetadataNonPojoImpl;
import org.hibernate.type.Type;
import org.hibernate.type.VersionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.util.Assert;

import adn.application.context.ContextProvider;
import adn.service.resource.local.factory.EntityManagerFactoryImplementor;
import adn.service.resource.metamodel.CentralAttributeContext;
import adn.service.resource.metamodel.EntityBinder;
import adn.service.resource.metamodel.EntityPersisterImplementor;
import adn.service.resource.metamodel.EntityTuplizerImplementor;
import adn.service.resource.metamodel.MetamodelImpl.IdentifierGenerationHolder;
import adn.service.resource.metamodel.MetamodelImpl.NoValueGeneration;
import adn.service.resource.metamodel.PropertyBinder;
import adn.service.resource.metamodel.ResourceType;
import adn.service.resource.metamodel.type.AbstractSyntheticBasicType;
import adn.service.resource.metamodel.type.ExplicitlyHydratedType;
import adn.service.resource.storage.LocalResourceStorage.ResultSetMetaDataImplementor;

/**
 * @author Ngoc Huy
 *
 */
public class ResourcePersisterImpl<D> implements ResourcePersister<D>, EntityPersisterImplementor<D>, ClassMetadata,
		SharedSessionUnwrapper, Lockable, Loadable {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final EntityManagerFactoryImplementor sessionFactory;
	private final ResourceType<D> metamodel;

	private String entityName;
	private Class<D> mappedClass;

	private SingularPersistentAttribute<D, ?> identifier;
	private PropertyAccess identifierAccess;
	private IdentifierGenerator identifierGenerator;

	private int propertySpan;
	private String[] propertyNames;
	private String[][] propertyColumnNames;
	private Map<String, Integer> indexMap = new HashMap<>();
	private PropertyAccess[] propertyAccesses;
	private Type[] propertyTypes;
	private ValueGeneration[] valueGenerations;
	private boolean[] autoGeneratedMarkers;
	private boolean[] propertyNullabilities;
	private boolean[] propertyUpdatabilities;
	private Instantiator instantiator;
	// Every resource instances is mutable
	private final EntityEntryFactory entryFactory = MutableEntityEntryFactory.INSTANCE;
	// Entity enhancement is not necessary
	private BytecodeEnhancementMetadata nonEnhanced;

	private final Map<LockMode, LockingStrategy> lockingStrategyMap = new HashMap<>();

	private UniqueEntityLoader resourceLoader;

	public ResourcePersisterImpl(EntityManagerFactoryImplementor sessionFactory, ResourceType<D> metamodel) {
		// TODO Auto-generated constructor stub
		Assert.notNull(sessionFactory, "ResourceManagerFactory must not be null");
		Assert.notNull(metamodel, "EntityTypeImpl must not be null");
		this.sessionFactory = sessionFactory;
		this.metamodel = metamodel;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void generateEntityDefinition() {
		mappedClass = metamodel.getJavaType();
		logger.trace("Generating entity definition of type " + mappedClass.getName());
		entityName = metamodel.getName();
		nonEnhanced = new BytecodeEnhancementMetadataNonPojoImpl(entityName);
		propertySpan = determinePropertySpan(metamodel);
		propertyNames = new String[propertySpan];
		propertyColumnNames = new String[propertySpan][1];
		propertyAccesses = new PropertyAccess[propertySpan];
		valueGenerations = new ValueGeneration[propertySpan];
		propertyTypes = new Type[propertySpan];
		autoGeneratedMarkers = new boolean[propertySpan];
		propertyNullabilities = new boolean[propertySpan];
		propertyUpdatabilities = new boolean[propertySpan];
		instantiator = new PojoInstantiator(metamodel.getJavaType(), null);

		try {
			instantiator.instantiate();
		} catch (InstantiationException ie) {
			throw new PropertyNotFoundException(
					"Resource of type " + getEntityName() + " must define a default constructor");
		}

		identifier = (SingularPersistentAttribute<D, ?>) metamodel.getId(metamodel.getIdType().getJavaType());
		Assert.notNull(identifier, "Unable to locate IDENTIFER for metamodel " + metamodel.getName());

		CentralAttributeContext attributeContext = sessionFactory.getContextBuildingService()
				.getService(CentralAttributeContext.class);
		ValueGeneration delegateGeneration;

		Assert.notNull(attributeContext, "Unable to locate CentricAttributeContext");

		ResourceType<? super D> superType = metamodel.locateSuperType();

		if (superType != null) {
			for (Attribute<? super D, ?> attr : superType.getAttributes()) {
				logger.trace(String.format("Retreiving metadata of attribute {%s} from {%s}", attr.getName(),
						superType.getName()));

				int propertyIndex = locateSuperPersister().getPropertyIndex(attr.getName());

				putProperty(attr.getName(), propertyIndex);
				propertyNames[propertyIndex] = locateSuperPersister().getPropertyNames()[propertyIndex];
				propertyAccesses[propertyIndex] = locatePropertyAccess(attr.getName());
				valueGenerations[propertyIndex] = (delegateGeneration = locateValueGeneration(attr.getName()));
				propertyTypes[propertyIndex] = locatePropertyType(attr.getName());
				propertyColumnNames[propertyIndex] = sessionFactory.getMetamodel()
						.entityPersister(metamodel.locateSuperType().getName()).getPropertyColumnNames(attr.getName());
			}
		}

		Attribute<?, ?>[] declaredAttributes = metamodel.getDeclaredAttributes().toArray(Attribute[]::new);

		for (int i = (superType == null ? 0 : superType.getAttributes().size()), j = 0; i < propertySpan; i++, j++) {
			Attribute<D, ?> attr = (Attribute<D, ?>) declaredAttributes[j];

			logger.trace(
					String.format("Creating metadata for %s.%s index {%d}", metamodel.getName(), attr.getName(), i));

			putProperty(attr.getName(), i);
			propertyNames[i] = attr.getName();
			propertyAccesses[i] = PropertyBinder.INSTANCE.createPropertyAccess(mappedClass, attr.getName(),
					attr.getJavaType());
			valueGenerations[i] = (delegateGeneration = PropertyBinder.INSTANCE.resolveValueGeneration(metamodel,
					attr));
			propertyTypes[i] = attributeContext.resolveType(metamodel, attr);
			propertyColumnNames[i] = new String[] { propertyTypes[i] instanceof AbstractSyntheticBasicType
					? propertyTypes[i] instanceof ExplicitlyHydratedType
							? PropertyBinder.INSTANCE.resolveSyntheticPropertyName("explicitly_hydrated", attr)
							: PropertyBinder.INSTANCE.resolveSyntheticPropertyName(
									PropertyBinder.INSTANCE.resolveBasicPropertyName(attr), attr)
					: PropertyBinder.INSTANCE.resolveBasicPropertyName(attr) };

			Assert.notNull(propertyAccesses[i],
					String.format("%s Unable to locate PropertyAccess for property %s", entityName, attr.getName()));
			Assert.notNull(valueGenerations[i],
					String.format("%s Unable to locate ValueGeneration for property %s", entityName, attr.getName()));
			Assert.notNull(propertyTypes[i],
					String.format("%s Unable to locate %s for property %s", entityName, Type.class, attr.getName()));

			propertyNullabilities[i] = attr instanceof SingularAttribute
					? ((SingularAttribute<? super D, ?>) attr).isOptional()
					: Optional
							.ofNullable(attr.getJavaMember() instanceof Field
									? PropertyBinder.INSTANCE.isOptional((Field) attr.getJavaMember())
									: null)
							.orElseThrow(
									() -> new IllegalArgumentException("Unable to determine nullability of attribute "
											+ attr.getName() + ", javatype " + attr.getJavaType()));
			propertyUpdatabilities[i] = attr.getJavaMember() instanceof Field
					? PropertyBinder.INSTANCE.isUpdatable((Field) attr.getJavaMember())
					: true;
			logger.trace(String.format(
					"Located valuegeneration of attribute %s. GenerationTiming %s. ValueGenerator %s", attr.getName(),
					delegateGeneration == IdentifierGenerationHolder.INSTANCE ? "determined by IdentifierGenerator"
							: delegateGeneration.getGenerationTiming(),
					delegateGeneration.getValueGenerator() != null
							? delegateGeneration.getValueGenerator().getClass().getName()
							: delegateGeneration == IdentifierGenerationHolder.INSTANCE
									? "determined by IdentifierGenerator"
									: "NULL"));
			autoGeneratedMarkers[i] = (delegateGeneration != NoValueGeneration.INSTANCE);
		}

		indexMap = indexMap.entrySet().stream().sorted((l, r) -> Integer.compare(l.getValue(), r.getValue()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		declaredAttributes = metamodel.getDeclaredAttributes().toArray(Attribute<?, ?>[]::new);

		String name;

		for (int i = 0; i < declaredAttributes.length; i++) {
			name = getPropertyColumnNames(getPropertyIndex(declaredAttributes[i].getName()))[0];

			try {
				logger.trace(String.format("Registering column %s into %s by [%s]", declaredAttributes[i].getName(),
						ResultSetMetaDataImplementor.class, name));
				addColumnToResultSetMetadata(name, getPropertyType(declaredAttributes[i].getName()));
			} catch (IllegalAccessException | NoSuchFieldException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				SpringApplication.exit(ContextProvider.getApplicationContext());
			}
		}

		if (!metamodel.hasSingleIdAttribute()) {
			ResourceType<? super D> root = metamodel;

			while (root.hasSingleIdAttribute() == false) {
				root = root.locateSuperType();
			}

			if (root == null || root.getName() == metamodel.getName()) {
				throw new IllegalArgumentException("Unable to locate identifier access for type " + entityName);
			}

			logger.trace("Inheriting IdentifierAccess from root named " + root.getName());
			identifierAccess = sessionFactory.getMetamodel().entityPersister(root.getName())
					.getPropertyAccess(getIdentifierPropertyName());
		} else {
			logger.trace("Creating IdentifierAccess");
			identifierAccess = propertyAccesses[indexMap
					.get(metamodel.getId(metamodel.getIdType().getJavaType()).getName())];

			try {
				identifierGenerator = EntityBinder.INSTANCE.locateIdentifierGenerator(metamodel, sessionFactory);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				SpringApplication.exit(ContextProvider.getApplicationContext());
			}
		}

		resourceLoader = new ResourceLoader(this);
	}

	private void putProperty(String name, int index) {
		logger.trace(String.format("Putting property {%s} -> {%d}", name, index));
		indexMap.put(name, index);
	}

	private int determinePropertySpan(ResourceType<? super D> metamodel) {
		if (metamodel.getSupertype() == null) {
			return metamodel.getDeclaredAttributes().size();
		}

		return metamodel.getDeclaredAttributes().size() + determinePropertySpan(metamodel.locateSuperType());
	}

	private LockingStrategy createLockingStrategy(LockMode lockMode) {
		return sessionFactory.getDialect().getLockingStrategy(this, lockMode);
	}

	private void addColumnToResultSetMetadata(String attrName, Type type)
			throws IllegalAccessException, NoSuchFieldException, SecurityException {
		ResultSetMetaDataImplementor metadata = sessionFactory.getContextBuildingService()
				.getService(ResultSetMetaDataImplementor.class);
		if (type instanceof AbstractSyntheticBasicType) {
			if (type instanceof ExplicitlyHydratedType) {
				metadata.getAccess().addExplicitlyHydratedColumn(attrName);

				return;
			}

			metadata.getAccess().addSynthesizedColumn(attrName);

			return;
		}

		metadata.getAccess().addColumn(attrName);
	}

	@Override
	public void postInstantiate() throws MappingException {

		logger.trace("Finished instantiating ResourcePersister for resource named " + getEntityName());
	}

	@Override
	public EntityEntryFactory getEntityEntryFactory() {

		return entryFactory;
	}

	@Override
	public String getRootEntityName() {

		return metamodel.locateRootType().getName();
	}

	@Override
	public String getEntityName() {

		return entityName;
	}

	@Override
	public boolean isSubclassEntityName(String entityName) {

		return metamodel.getSubclassNames().contains(entityName);
	}

	@Override
	public Serializable[] getPropertySpaces() {

		return new Serializable[0];
	}

	@Override
	public Serializable[] getQuerySpaces() {

		return new Serializable[0];
	}

	@Override
	public boolean hasProxy() {

		return false;
	}

	@Override
	public boolean hasCollections() {

		return false;
	}

	@Override
	public boolean hasMutableProperties() {

		return IntStream.range(0, propertySpan).mapToObj(index -> propertyUpdatabilities[index])
				.filter(pred -> pred == true).count() > 0;
	}

	@Override
	public boolean hasSubselectLoadableCollections() {

		return false;
	}

	@Override
	public boolean hasCascades() {

		return false;
	}

	@Override
	public boolean isMutable() {

		return entryFactory.getClass().equals(MutableEntityEntryFactory.class);
	}

	@Override
	public boolean isInherited() {

		return metamodel.getSuperType() != null;
	}

	@Override
	public boolean isIdentifierAssignedByInsert() {

		return identifierGenerator != null && getValueGeneration(metamodel.locateIdAttribute().getName())
				.getGenerationTiming() == GenerationTiming.INSERT;
	}

	@Override
	public Type getPropertyType(String propertyName) throws MappingException {
		if (!indexMap.containsKey(propertyName)) {
			return null;
		}

		return propertyTypes[indexMap.get(propertyName)];
	}

	@Override
	public Type locatePropertyType(String propertyName) {
		Type candidate = getPropertyType(propertyName);

		if (candidate != null) {
			return candidate;
		}

		if (metamodel.getSupertype() == null) {
			return null;
		}

		return sessionFactory.getMetamodel().entityPersister(metamodel.locateSuperType().getName())
				.locatePropertyType(propertyName);
	}

	@Override
	public int[] findDirty(Object[] currentState, Object[] previousState, Object owner,
			SharedSessionContractImplementor session) {

		int[] indices = null;
		int count = 0;

		for (int i = 0; i < propertySpan; i++) {
			if (propertyTypes[i].isDirty(previousState[i], currentState[i], session)) {
				indices = nullableGet(indices, count, i);
			}
		}

		return indices == null ? null : ArrayHelper.trim(indices, count);
	}

	private int[] nullableGet(int[] indices, int index, int val) {
		if (indices == null) {
			indices = new int[propertySpan];

			indices[0] = val;

			return indices;
		}

		indices[index] = val;

		return indices;
	}

	@Override
	public int[] findModified(Object[] old, Object[] current, Object object, SharedSessionContractImplementor session) {

		return findDirty(current, current, object, session);
	}

	@Override
	public boolean hasIdentifierProperty() {

		return metamodel.hasSingleIdAttribute();
	}

	@Override
	public boolean canExtractIdOutOfEntity() {

		return identifierAccess != null;
	}

	@Override
	public boolean isVersioned() {

		return metamodel.hasVersionAttribute();
	}

	@Override
	public VersionType<?> getVersionType() {

		if (!isVersioned()) {
			return null;
		}

		Type candidate = propertyTypes[indexMap.get(metamodel.locateVersionAttribute().getName())];

		if (candidate instanceof VersionType) {
			return (VersionType<?>) candidate;
		}

		throw new IllegalArgumentException("Unable to locate VersionType");
	}

	@Override
	public int getVersionProperty() {

		return indexMap.get(metamodel.locateVersionAttribute().getName());
	}

	@Override
	public boolean hasNaturalIdentifier() {
		// always lookup by identifier
		return false;
	}

	@Override
	public int[] getNaturalIdentifierProperties() {

		if (!hasNaturalIdentifier()) {
			return null;
		}

		return new int[] { indexMap.get(metamodel.locateIdAttribute().getName()) };
	}

	@Override
	public Object[] getNaturalIdentifierSnapshot(Serializable id, SharedSessionContractImplementor session) {

		return null;
	}

	@Override
	public IdentifierGenerator getIdentifierGenerator() {

		return identifierGenerator;
	}

	@Override
	public boolean hasLazyProperties() {

		return false;
	}

	private void assertNaturalId(Object[] values) {
		Assert.isTrue(values.length == 1, "Plural value id is not supported");
		Assert.isTrue(values[0] instanceof Serializable, "Natural id value must be Serializable");
	}

	@Override
	public Serializable loadEntityIdByNaturalId(Object[] naturalIdValues, LockOptions lockOptions,
			SharedSessionContractImplementor session) {

		assertNaturalId(naturalIdValues);
		// directly load from storage
		Object instance = getFactory().getStorage().select((Serializable) naturalIdValues[0]);

		if (instance == null) {
			return null;
		}

		return (Serializable) getIdentifierType().resolve(getIdentifier(instance), session, instance);
	}

	@Override
	public Object load(Serializable id, Object optionalObject, LockMode lockMode,
			SharedSessionContractImplementor session) throws HibernateException {

		return load(id, optionalObject, new LockOptions().setLockMode(lockMode), null);
	}

	@Override
	public Object load(Serializable id, Object optionalObject, LockOptions lockOptions,
			SharedSessionContractImplementor session) throws HibernateException {
		return resourceLoader.load(id, optionalObject, session, lockOptions);
	}

	@Override
	public List<?> multiLoad(Serializable[] ids, SharedSessionContractImplementor session,
			MultiLoadOptions loadOptions) {

		return null;
	}

	@Override
	public void lock(Serializable id, Object version, Object object, LockMode lockMode,
			SharedSessionContractImplementor session) throws HibernateException {

		locateLockingStrategy(lockMode).lock(id, version, object, LockOptions.WAIT_FOREVER, session);
	}

	@Override
	public void lock(Serializable id, Object version, Object object, LockOptions lockOptions,
			SharedSessionContractImplementor session) throws HibernateException {

		locateLockingStrategy(lockOptions.getLockMode()).lock(id, version, object, lockOptions.getTimeOut(), session);
	}

	private LockingStrategy locateLockingStrategy(LockMode lockMode) {
		return lockingStrategyMap.computeIfAbsent(lockMode, this::createLockingStrategy);
	}

	@Override
	public void insert(Serializable id, Object[] fields, Object object, SharedSessionContractImplementor session)
			throws HibernateException {

	}

	@Override
	public Serializable insert(Object[] fields, Object object, SharedSessionContractImplementor session)
			throws HibernateException {

		return null;
	}

	@Override
	public void delete(Serializable id, Object version, Object object, SharedSessionContractImplementor session)
			throws HibernateException {

	}

	@Override
	public void update(Serializable id, Object[] fields, int[] dirtyFields, boolean hasDirtyCollection,
			Object[] oldFields, Object oldVersion, Object object, Object rowId,
			SharedSessionContractImplementor session) throws HibernateException {

	}

	@Override
	public Type[] getPropertyTypes() {
		return propertyTypes;
	}

	@Override
	public String[] getPropertyNames() {
		return propertyNames;
	}

	@Override
	public boolean[] getPropertyInsertability() {
		boolean[] arr = new boolean[propertySpan];

		Arrays.fill(arr, true);

		return arr;
	}

	@Override
	public ValueInclusion[] getPropertyInsertGenerationInclusions() {

		ValueInclusion[] arr = new ValueInclusion[propertySpan];

		Arrays.fill(arr, ValueInclusion.FULL);

		return arr;
	}

	@Override
	public ValueInclusion[] getPropertyUpdateGenerationInclusions() {
		return Stream.of(valueGenerations)
				.map(vg -> vg != NoValueGeneration.INSTANCE && vg.getGenerationTiming().equals(GenerationTiming.ALWAYS)
						? ValueInclusion.FULL
						: ValueInclusion.NONE)
				.toArray(ValueInclusion[]::new);
	}

	@Override
	public boolean[] getPropertyUpdateability() {

		return propertyUpdatabilities;
	}

	@Override
	public boolean[] getPropertyCheckability() {

		boolean[] arr = new boolean[propertySpan];

		Arrays.fill(arr, true);

		return arr;
	}

	@Override
	public boolean[] getPropertyNullability() {

		return propertyNullabilities;
	}

	@Override
	public boolean[] getPropertyVersionability() {

		return null;
	}

	@Override
	public boolean[] getPropertyLaziness() {

		boolean[] arr = new boolean[propertySpan];

		Arrays.fill(arr, false);

		return arr;
	}

	@Override
	public CascadeStyle[] getPropertyCascadeStyles() {

		return null;
	}

	@Override
	public Type getIdentifierType() {
		if (!hasIdentifierProperty()) {
			return locateSuperPersister().getIdentifierType();
		}

		return propertyTypes[indexMap.get(getIdentifierPropertyName())];
	}

	@SuppressWarnings("unchecked")
	private ResourcePersister<? super D> locateSuperPersister() {
		if (metamodel.getSupertype() != null) {
			return (ResourcePersister<? super D>) sessionFactory.getMetamodel()
					.locateEntityPersister(metamodel.locateSuperType().getName());
		}

		return null;
	}

	@Override
	public String getIdentifierPropertyName() {
		return identifier.getName();
	}

	@Override
	public ClassMetadata getClassMetadata() {

		return this;
	}

	@Override
	public boolean isBatchLoadable() {

		return true;
	}

	@Override
	public boolean isSelectBeforeUpdateRequired() {

		return false;
	}

	@Override
	public Object[] getDatabaseSnapshot(Serializable id, SharedSessionContractImplementor session)
			throws HibernateException {

		return null;
	}

	@Override
	public Serializable getIdByUniqueKey(Serializable key, String uniquePropertyName,
			SharedSessionContractImplementor session) {

		return null;
	}

	@Override
	public Object getCurrentVersion(Serializable id, SharedSessionContractImplementor session)
			throws HibernateException {

		return null;
	}

	@Override
	public Object forceVersionIncrement(Serializable id, Object currentVersion,
			SharedSessionContractImplementor session) throws HibernateException {
		return valueGenerations[getVersionProperty()].getGenerationTiming() == GenerationTiming.ALWAYS;
	}

	@Override
	public boolean isInstrumented() {

		return false;
	}

	@Override
	public boolean hasInsertGeneratedProperties() {
		return Stream.of(valueGenerations).filter(vg -> vg.getGenerationTiming() == GenerationTiming.INSERT)
				.count() != 0;
	}

	@Override
	public boolean hasUpdateGeneratedProperties() {
		return Stream.of(valueGenerations).filter(vg -> vg.getGenerationTiming() == GenerationTiming.ALWAYS)
				.count() != 0;
	}

	@Override
	public boolean isVersionPropertyGenerated() {
		return valueGenerations[getVersionProperty()] != NoValueGeneration.INSTANCE;
	}

	@Override
	public void afterInitialize(Object entity, SharedSessionContractImplementor session) {
		logger.trace("Finished initializing entity " + entity.toString());
	}

	@Override
	public void afterReassociate(Object entity, SharedSessionContractImplementor session) {

	}

	@Override
	public Object createProxy(Serializable id, SharedSessionContractImplementor session) throws HibernateException {

		return null;
	}

	@Override
	public Boolean isTransient(Object object, SharedSessionContractImplementor session) throws HibernateException {

		return null;
	}

	@Override
	public Object[] getPropertyValuesToInsert(Object object, @SuppressWarnings("rawtypes") Map mergeMap,
			SharedSessionContractImplementor session) throws HibernateException {

		return null;
	}

	@Override
	public void processInsertGeneratedProperties(Serializable id, Object entity, Object[] state,
			SharedSessionContractImplementor session) {

	}

	@Override
	public void processUpdateGeneratedProperties(Serializable id, Object entity, Object[] state,
			SharedSessionContractImplementor session) {

	}

	@Override
	public Class<D> getMappedClass() {

		return mappedClass;
	}

	@Override
	public boolean implementsLifecycle() {

		return false;
	}

	@Override
	public Class<?> getConcreteProxyClass() {

		return mappedClass;
	}

	private void assertInput(Object[] values) {
		Assert.isTrue(values.length == propertySpan, "Input values length and property span must match");
	}

	@Override
	public void setPropertyValues(Object object, Object[] values) {
		assertInput(values);

		for (int i = 0; i < propertySpan; i++) {
			setPropertyValue(object, i, values[i]);
		}
	}

	private void assertIndex(int i) {
		Assert.isTrue(i < propertySpan, "Index exceeded");
	}

	@SuppressWarnings("unchecked")
	private void assertValue(int i, Object value) {
		assertIndex(i);
		Assert.isTrue(
				propertyTypes[i].getReturnedClass() == value.getClass()
						|| propertyTypes[i].getReturnedClass().isAssignableFrom(value.getClass()),
				String.format("Value type and returned type must match [%s><%s]", value.getClass(),
						propertyTypes[i].getReturnedClass()));
	}

	@Override
	public void setPropertyValue(Object object, int i, Object value) {
		assertValue(i, value);
		propertyAccesses[i].getSetter().set(object, value, getFactory());
	}

	@Override
	public Object[] getPropertyValues(Object object) {

		return Stream.of(propertyAccesses).map(pa -> pa.getGetter().get(object)).toArray();
	}

	@Override
	public Object getPropertyValue(Object object, int i) throws HibernateException {
		assertIndex(i);
		return propertyAccesses[i].getGetter().get(object);
	}

	@Override
	public Object getPropertyValue(Object object, String propertyName) {
		return getPropertyValue(object, indexMap.get(propertyName));
	}

	@Override
	public Serializable getIdentifier(Object object) throws HibernateException {
		if (canExtractIdOutOfEntity()) {
			return (Serializable) identifierAccess.getGetter().get(object);
		}

		throw new HibernateException(String.format("%s Can not extract id out of entity", entityName));
	}

	@Override
	public Serializable getIdentifier(Object entity, SharedSessionContractImplementor session) {
		return getIdentifier(entity);
	}

	private void assertIdValue(Serializable id) {
		Assert.isTrue(
				id.getClass() == getIdentifierType().getReturnedClass()
						|| id.getClass().isAssignableFrom(getIdentifierType().getReturnedClass()),
				"Id input and returned class must match");
	}

	@Override
	public void setIdentifier(Object entity, Serializable id, SharedSessionContractImplementor session) {
		assertIdValue(id);
		identifierAccess.getSetter().set(entity, id, getFactory());
	}

	@Override
	public Object getVersion(Object object) throws HibernateException {

		return propertyAccesses[getVersionProperty()].getGetter().get(object);
	}

	@Override
	public Object instantiate(Serializable id, SharedSessionContractImplementor session) {

		return instantiator.instantiate(id);
	}

	@Override
	public boolean isInstance(Object object) {
		return instantiator.isInstance(object);
	}

	@Override
	public boolean hasUninitializedLazyProperties(Object object) {

		return false;
	}

	@Override
	public void resetIdentifier(Object entity, Serializable currentId, Object currentVersion,
			SharedSessionContractImplementor session) {

	}

	@SuppressWarnings("unchecked")
	@Override
	public EntityTuplizerImplementor<D> getEntityTuplizer() {
		return this.unwrap(EntityTuplizerImplementor.class);
	}

	@Override
	public BytecodeEnhancementMetadata getInstrumentationMetadata() {
		return nonEnhanced;
	}

	@Override
	public FilterAliasGenerator getFilterAliasGenerator(String rootAlias) {

		return null;
	}

	@Override
	public int[] resolveAttributeIndexes(String[] attributeNames) {

		Assert.isTrue(attributeNames.length <= propertySpan, "Property span exceeded");
		int[] arr = new int[attributeNames.length];
		int i = 0;

		for (String name : attributeNames) {
			arr[i++] = Optional.ofNullable(indexMap.get(name)).orElseThrow();
		}

		return arr;
	}

	@Override
	public EntityIdentifierDefinition getEntityKeyDefinition() {

		return null;
	}

	@Override
	public Iterable<AttributeDefinition> getAttributes() {

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResourcePersister<D> getSubclassEntityPersister(Object instance, SessionFactoryImplementor factory) {

		return (ResourcePersister<D>) metamodel.getSubclassNames().stream()
				.map(name -> sessionFactory.getMetamodel().entityPersister(name))
				.filter(persister -> persister.isInstance(factory)).findFirst().orElse(null);
	}

	@Override
	public ResourcePersister<D> getEntityPersister() {

		return this;
	}

	@Override
	public boolean hasSubclasses() {

		return metamodel.hasSubclasses();
	}

	@Override
	public void setPropertyValue(Object object, String propertyName, Object value) throws HibernateException {

		setPropertyValue(object, indexMap.get(propertyName), value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E unwrap(Class<E> type) {

		return (E) this;
	}

	@Override
	public String toString() {
		// @formatter:off
		return String.format("Metamodel: %s\n"
				+ "\t-propertySpan: %d\n"
				+ "\t-indexMap: [%s]\n"
				+ "\t-valueGenerations: [%s]\n"
				+ "\t-propertyNullabilities: [%s]\n"
				+ "\t-propertyUpdatabilities: [%s]\n"
				+ "\t-propertyTypes: [%s]\n"
				+ "\t-getters: [%s]\n"
				+ "\t-setters: [%s]\n"
				+ "\t-autoGeneratedMarkers: [%s]\n"
				+ "\t-identifierGenerator: %s\n"
				+ "\t-superType: %s\n"
				+ "\t-declaredAttributes: \n%s\n"
				+ "\t-declaredSingularAttributes: \n%s\n"
				+ "\t-declaredPluralAttributes: \n%s\n"
				+ "\t-hasIdentifierProperty: %b\n"
				+ "\t-hasIdClass: %b\n"
				+ "\t-id: %s\n"
				+ "\t-idClassAttributes(in size): %d\n"
				+ "\t-isVersioned: %b\n"
				+ "\t-versionAttribute: %s\n"
				+ "\t-subclassNames: %s",
				getEntityName(),
				propertySpan,
				indexMap.entrySet().stream().map(ele -> ele.getValue() + "|" + ele.getKey()).collect(Collectors.joining(", ")),
				Stream.of(valueGenerations)
					.map(ele -> {
						if (ele instanceof IdentifierGenerationHolder) {
							return "Determined by IdentifierGenerator|Generated by IdentifierGenerator";
						}
						
						return ele.getGenerationTiming() + "|" + (ele.getValueGenerator() != null ? ele.getValueGenerator().getClass().getName() : "NULL");
					})
					.collect(Collectors.joining(", ")),
				IntStream.range(0, propertySpan)
					.mapToObj(index -> String.valueOf(propertyNullabilities[index]))
					.collect(Collectors.joining(", ")),
				IntStream.range(0, propertySpan)
					.mapToObj(index -> String.valueOf(propertyUpdatabilities[index]))
					.collect(Collectors.joining(", ")),
				Stream.of(propertyTypes)
					.map(ele -> ele.getName())
					.collect(Collectors.joining(", ")),
				Stream.of(propertyAccesses)
					.map(pa -> pa.getGetter().getMethod().getName())
					.collect(Collectors.joining(", ")),
				Stream.of(propertyAccesses)
					.map(pa -> pa.getSetter().getMethod().getName())
					.collect(Collectors.joining(", ")),	
				IntStream.range(0, propertySpan)
					.mapToObj(index -> String.valueOf(autoGeneratedMarkers[index]))
					.collect(Collectors.joining(", ")),
				identifierGenerator != null ? identifierGenerator.getClass().getName() : "NULL",
				metamodel.getSupertype() != null ? metamodel.getSupertype().getName() : "NULL",
				metamodel.getDeclaredAttributes().stream().map(ele -> String.format(""
						+ "\t\t-name: %s\n"
						+ "\t\t-propertyColumnNames: %s\n"
						+ "\t\t-persistentAttributeType: %s\n"
						+ "\t\t-declaringType: %s\n"
						+ "\t\t-javaMember: %s\n"
						+ "\t\t-javaType: %s\n"
						+ "\t\t-isAssociation: %b\n"
						+ "\t\t-isCollection: %b\n"
						+ "\t\t-typeDescriptorClass: %s",
						ele.getName(),
						propertyColumnNames[indexMap.get(ele.getName())][0],
						ele.getPersistentAttributeType(),
						ele.getDeclaringType().getJavaType(),
						ele.getJavaMember().getName(),
						ele.getJavaType(),
						ele.isAssociation(),
						ele.isCollection(),
						(
							getPropertyType(ele.getName()) instanceof ExplicitlyHydratedType ?
									String.format("%s:%s", getPropertyType(ele.getName()).getClass(), getPropertyType(ele.getName()).getName()) :
								getPropertyType(ele.getName()).getClass()
								))).collect(Collectors.joining("\n\t\t--------------------\n")),
				metamodel.getDeclaredSingularAttributes().stream().map(ele -> String.format("\t\t-name: %s\n"
						+ "\t\t-isId: %b\n"
						+ "\t\t-isVersion: %b\n"
						+ "\t\t-isOptional: %b", 
						ele.getName(),
						ele.isId(),
						ele.isVersion(),
						ele.isOptional())).collect(Collectors.joining("\n\t\t--------------------\n")),
				metamodel.getDeclaredPluralAttributes().stream().map(ele -> String.format("\t\t-name: %s\n"
						+ "\t\t-collectionType: %s\n"
						+ "\t\t-elementType: %s\n",
						ele.getName(),
						ele.getCollectionType(),
						ele.getElementType().getJavaType().getName())).collect(Collectors.joining("\n\t\t--------------------\n")),
				metamodel.hasSingleIdAttribute(),
				metamodel.hasIdClass(),
				metamodel.hasSingleIdAttribute() ? metamodel.getId(metamodel.getIdType().getJavaType()).getName() + ": " + metamodel.getId(metamodel.getIdType().getJavaType()).getJavaType().getName() : "NULL",
				metamodel.hasIdClass() ? metamodel.getIdClassAttributes().size() : 0,
				metamodel.hasVersionAttribute(),
				metamodel.hasVersionAttribute() ? metamodel.getDeclaredVersion().getName() + ": " + metamodel.getDeclaredVersion().getJavaType().getName() : "NULL",
				metamodel.getSubclassNames().stream().collect(Collectors.joining(", ")));
		// @formatter:on
	}

	@Override
	public PropertyAccess getPropertyAccess(String propertyName) {
		if (!indexMap.containsKey(propertyName)) {
			return null;
		}

		return getPropertyAccess(indexMap.get(propertyName));
	}

	@Override
	public PropertyAccess getPropertyAccess(int propertyIndex) {
		return propertyAccesses[propertyIndex];
	}

	@Override
	public PropertyAccess locatePropertyAccess(String propertyName) {
		PropertyAccess candidate = getPropertyAccess(propertyName);

		if (candidate != null) {
			return candidate;
		}

		if (metamodel.getSupertype() == null) {
			return null;
		}

		return sessionFactory.getMetamodel().entityPersister(metamodel.locateSuperType().getName())
				.locatePropertyAccess(propertyName);
	}

	@Override
	public ValueGeneration getValueGeneration(int propertyIndex) {

		return valueGenerations[propertyIndex];
	}

	@Override
	public ValueGeneration getValueGeneration(String propertyName) {
		if (!indexMap.containsKey(propertyName)) {
			return null;
		}

		return getValueGeneration(indexMap.get(propertyName));
	}

	@Override
	public ValueGeneration locateValueGeneration(String propertyName) {
		ValueGeneration candidate = getValueGeneration(propertyName);

		if (candidate != null) {
			return candidate;
		}

		if (metamodel.getSupertype() == null) {
			return null;
		}

		return sessionFactory.getMetamodel().entityPersister(metamodel.locateSuperType().getName())
				.locateValueGeneration(propertyName);
	}

	@Override
	public String getRootTableName() {

		return metamodel.locateRootType().getName();
	}

	@Override
	public String getRootTableAlias(String drivingAlias) {

		return null;
	}

	@Override
	public String[] getRootTableIdentifierColumnNames() {

		return new String[] { metamodel.locateRootType().locateIdAttribute().getName() };
	}

	@Override
	public String getVersionColumnName() {

		if (!isVersioned()) {
			return null;
		}

		return getPropertyNames()[getVersionProperty()];
	}

	@Override
	public Type getDiscriminatorType() {
		return null;
	}

	@Override
	public Object getDiscriminatorValue() {
		return null;
	}

	@Override
	public String getSubclassForDiscriminatorValue(Object value) {
		return null;
	}

	@Override
	public String[] getIdentifierColumnNames() {
		return getPropertyColumnNames(indexMap.get(identifier.getName()));
	}

	@Override
	public String[] getIdentifierAliases(String suffix) {
		return getIdentifierColumnNames();
	}

	@Override
	public String[] getPropertyAliases(String suffix, int i) {
		return null;
	}

	@Override
	public String[] getPropertyColumnNames(int i) {
		return propertyColumnNames[i];
	}

	@Override
	public String[] getPropertyColumnNames(String propertyName) {
		// TODO Auto-generated method stub
		return propertyColumnNames[indexMap.get(propertyName)];
	}

	@Override
	public String getDiscriminatorAlias(String suffix) {

		return null;
	}

	@Override
	public String getDiscriminatorColumnName() {

		return null;
	}

	@Override
	public boolean hasRowId() {

		return false;
	}

	@Override
	public Object[] hydrate(ResultSet rs, Serializable id, Object object, Loadable rootLoadable,
			String[][] suffixedPropertyColumns, boolean allProperties, SharedSessionContractImplementor session)
			throws SQLException, HibernateException {
		// TODO: hydrate
		logger.debug(String.format("[Row-Hydrate]"));

		int n = propertyTypes.length;
		Object[] values = new Object[n];

		for (int i = 0; i < n; i++) {
			values[i] = propertyTypes[i].hydrate(rs, getPropertyColumnNames(i), session, object);
		}

		return values;
	}

	@Override
	public boolean isAbstract() {

		return false;
	}

	@Override
	public void registerAffectingFetchProfile(String fetchProfileName) {

	}

	@Override
	public String getTableAliasForColumn(String columnName, String rootAlias) {

		return null;
	}

	@Override
	public EntityManagerFactoryImplementor getFactory() {
		return sessionFactory;
	}

	@Override
	public int getPropertyIndex(String propertyName) {
		return indexMap.get(propertyName);
	}

}
