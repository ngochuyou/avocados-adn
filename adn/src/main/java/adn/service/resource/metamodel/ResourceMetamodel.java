/**
 * 
 */
package adn.service.resource.metamodel;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.service.resource.OptimisticLockStyle;
import adn.service.resource.persister.ResourcePersister;
import adn.service.resource.tuple.AbstractAttribute;
import adn.service.resource.tuple.IdentifierProperty;
import adn.service.resource.tuple.IdentifierValue;
import adn.service.resource.tuple.PojoResourceTuplizer;
import adn.service.resource.tuple.ResourceTuplizer;
import adn.service.resource.tuple.VersionProperty;
import adn.service.resource.tuple.VersionValue;
import adn.utilities.TypeUtils;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceMetamodel {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final EntityManager resourceManager;

	private static final int NO_VERSION_INDEX = -666;

	private final Class<?> mappedClass;
	private final String name;
	private final String rootName;

	private final IdentifierProperty<?, ?> identifierAttribute;
	private final boolean versioned;
	private final VersionProperty<?, ?> versionAttribute;

	private final int propertySpan;
	private final int versionPropertyIndex;
	private final Attribute<?, ?>[] properties;

	private final String[] propertyNames;
	private final Type<?>[] propertyTypes;
	private final boolean[] propertyUpdatability;
	private final boolean[] propertyNullability;
	private final boolean[] propertyCheckability;
	private final boolean[] propertyInsertability;
	private final boolean[] propertyVersionability;

	private final Map<String, Integer> propertyIndices = new HashMap<>();
	private final boolean hasCollections;
	private final BitSet mutablePropertiesIndexes;

	private final boolean isAbstract;
	private final OptimisticLockStyle optimisticLockStyle;

	private final boolean mutable;
	private final boolean polymorphic;
	private final String superClass;
	private final boolean explicitPolymorphism;
	private final boolean inherited;
	private final boolean hasSubclasses;
	private final Set<String> subclassEntityNames;

	private final EntityMode mode;
	private final ResourceTuplizer tuplizer;

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <X> ResourceMetamodel(ResourceTypeImpl<X> type, ResourcePersister persister, EntityManager resourceManager)
			throws IllegalArgumentException {
		// TODO Auto-generated constructor stub
		this.resourceManager = resourceManager;

		mappedClass = type.getJavaType();
		name = type.getName();

		AbstractManagedType<? super X> superType = type.getSuperType();

		while (superType != null && superType.getSuperType() != null) {
			superType = superType.getSuperType();
		}

		rootName = superType == null ? null : superType.getName();
		inherited = superType == null ? false : true;

		versioned = type.hasVersionAttribute();

		SingularAttribute<? super X, ?> id;

		identifierAttribute = new IdentifierProperty<>((id = type.getId(type.getIdType().getJavaType())).getName(),
				id.getType(), id.getType() instanceof EmbeddableTypeImpl, IdentifierValue.NULL, null, type,
				id.getPersistentAttributeType());

		SingularAttribute<? super X, ?> version = type.getVersion();

		versionAttribute = versioned
				? new VersionProperty<>(version.getName(), version.getType(), version.getDeclaringType(),
						VersionValue.NULL, version.getPersistentAttributeType())
				: null;

		Field[] fields = TypeUtils.getAllFields(type.getJavaType());

		properties = type.getAttributes().toArray(Attribute<?, ?>[]::new);

		visitAttributes(properties, p -> p instanceof AbstractAttribute);

		propertySpan = properties.length;
		versionPropertyIndex = getAttributeIndex(fields, type.getVersion().getName());

		propertyNames = Stream.of(properties).map(p -> p.getName()).toArray(String[]::new);
		propertyTypes = Stream.of(properties).map(p -> {
			return ((AbstractAttribute<X, ?>) p).getAttributeType();
		}).toArray(Type<?>[]::new);

		propertyUpdatability = new boolean[propertySpan];
		propertyInsertability = new boolean[propertySpan];
		propertyCheckability = new boolean[propertySpan];
		Arrays.fill(propertyCheckability, false);
		propertyNullability = new boolean[propertySpan];
		propertyVersionability = new boolean[propertySpan];

		BitSet mutableIndexes = new BitSet();
		AbstractAttribute<X, ?> casted;
		boolean foundCollections = false;

		for (int i = 0; i < propertySpan; i++) {
			casted = (AbstractAttribute<X, ?>) properties[i];
			propertyUpdatability[i] = casted.isUpdatable();
			propertyNullability[i] = casted.isNullable();
			propertyInsertability[i] = casted.isInsertable();
			propertyVersionability[i] = casted.isVersionable();
			propertyIndices.put(casted.getAttributeName(), i);
			foundCollections = foundCollections || Collection.class.isAssignableFrom(casted.getJavaType());

			if (((AbstractType<?>) ((AbstractAttribute<?, ?>) propertyTypes[i]).getAttributeType()).isMutable()
					&& propertyCheckability[i]) {
				mutableIndexes.set(i);
			}
		}

		mutable = type.isMutable();
		hasCollections = foundCollections;
		mutablePropertiesIndexes = new BitSet(propertySpan);
		isAbstract = type.isAbstract();
		optimisticLockStyle = type.getOptimisticLockStyle();
		polymorphic = type.isPolymorphic();
		hasSubclasses = type.hasSubtypes();
		superClass = !inherited ? null : type.getSuperType().getName();
		explicitPolymorphism = true;
		subclassEntityNames = type.getSubtypes().stream().map(t -> t.getName()).collect(Collectors.toSet());

		mode = type.hasPojo() ? EntityMode.POJO : EntityMode.MAP;

		if (mode != EntityMode.POJO) {
			throw new IllegalStateException("At the momment, only Entity of mode POJO is supported");
		}

		tuplizer = new PojoResourceTuplizer(this);

		logger.debug(toString());
	}

	private int getAttributeIndex(Field[] fields, String propertyName) throws IllegalArgumentException {
		int n = fields.length;

		for (int i = 0; i < n; i++) {
			if (fields[i].getName() == propertyName) {
				return i;
			}
		}

		return NO_VERSION_INDEX;
	}

	private void visitAttributes(Attribute<?, ?>[] attributes, Function<Attribute<?, ?>, Boolean> visitor)
			throws IllegalArgumentException {
		for (Attribute<?, ?> attr : attributes) {
			if (!visitor.apply(attr)) {
				throw new IllegalArgumentException("Assertion failed on attribute: " + attr.getName());
			}
		}
	}

	public EntityMode getMode() {
		return mode;
	}

	public ResourceTuplizer getTuplizer() {
		return tuplizer;
	}

	public EntityManager getResourceManager() {
		return resourceManager;
	}

	public String getName() {
		return name;
	}

	public String getRootName() {
		return rootName;
	}

	public IdentifierProperty<?, ?> getIdentifierAttribute() {
		return identifierAttribute;
	}

	public boolean isVersioned() {
		return versioned;
	}

	public int getPropertySpan() {
		return propertySpan;
	}

	public int getVersionPropertyIndex() {
		return versionPropertyIndex;
	}

	public Attribute<?, ?>[] getProperties() {
		return properties;
	}

	public String[] getPropertyNames() {
		return propertyNames;
	}

	public Type<?>[] getPropertyTypes() {
		return propertyTypes;
	}

	public boolean[] getPropertyUpdateability() {
		return propertyUpdatability;
	}

	public boolean[] getPropertyNullability() {
		return propertyNullability;
	}

	public boolean[] getPropertyCheckability() {
		return propertyCheckability;
	}

	public boolean[] getPropertyInsertability() {
		return propertyInsertability;
	}

	public boolean[] getPropertyVersionability() {
		return propertyVersionability;
	}

	public Map<String, Integer> getPropertyIndexes() {
		return propertyIndices;
	}

	public boolean getHasCollections() {
		return hasCollections;
	}

	public BitSet getMutablePropertiesIndexes() {
		return mutablePropertiesIndexes;
	}

	public OptimisticLockStyle getOptimisticLockStyle() {
		return optimisticLockStyle;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public boolean isPolymorphic() {
		return polymorphic;
	}

	public String getSuperclass() {
		return superClass;
	}

	public boolean isExplicitPolymorphism() {
		return explicitPolymorphism;
	}

	public boolean isInherited() {
		return inherited;
	}

	public boolean isMutable() {
		// TODO Auto-generated method stub
		return mutable;
	}

	public boolean hasSubclasses() {
		return hasSubclasses;
	}

	public Set<String> getSubclassEntityNames() {
		return subclassEntityNames;
	}

	public Class<?> getMappedClass() {
		return mappedClass;
	}

	public int getPropertyIndex(String propertyName) {

		return propertyIndices.get(propertyName);
	}

	public IdentifierProperty<?, ?> getIdentifierProperty() {
		return identifierAttribute;
	}

	public Type<?> getPropertyType(String propertyName) {
		return propertyTypes[propertyIndices.get(propertyName)];
	}
	
	/**
	 * @return the versionAttribute
	 */
	public VersionProperty<?, ?> getVersionAttribute() {
		return versionAttribute;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		// @formatter:off
		return String.format("ResourceMetamodel for type: %s"
							+ "\t-name: %s"
							+ "\t-rootName: %s"
							+ "\t-identifierAttribute: %s"
							+ "\t-versioned: %b"
							+ "\t-propertySpan: %d"
							+ "\t-propertyIndicies: %s"
							+ "\t-propertyNames: [%s]"
							+ "\t-propertyTypes: [%s]"
							+ "\t-propertyUpdatability: [%s]"
							+ "\t-propertyNullability: [%s]"
							+ "\t-propertyCheckability: [%s]"
							+ "\t-propertyInsertability: [%s]"
							+ "\t-propertyVersionability: [%s]"
							+ "\t-hasCollections: %b"
							+ "\t-isAbstract: %b"
							+ "\t-optimisticLockStyle: %s"
							+ "\t-polymorphic: %b"
							+ "\t-superClass: %s"
							+ "\t-inherited: %b"
							+ "\t-hasSubclasses: %b"
							+ "\t-subclassEntityNames: %s"
							+ "\t-mode: %s"
							+ "\t-tuplizer: %s",
			mappedClass.getName(),
			name,
			rootName,
			identifierAttribute.getName(),
			versioned,
			propertySpan,
			propertyIndices.entrySet().stream().map(entry -> "[" + entry.getKey() + "|" + entry.getValue() + ']').collect(Collectors.joining(", ")),
			Stream.of(propertyNames).collect(Collectors.joining(", ")),
			Stream.of(propertyTypes).map(t -> t.getJavaType().getSimpleName()).collect(Collectors.joining(", ")),
			IntStream.range(0, propertyUpdatability.length).mapToObj(i -> propertyUpdatability[i])
					.map(t -> t.toString()).collect(Collectors.joining(", ")),
			IntStream.range(0, propertyNullability.length).mapToObj(i -> propertyNullability[i])
					.map(t -> t.toString()).collect(Collectors.joining(", ")),
			IntStream.range(0, propertyCheckability.length).mapToObj(i -> propertyCheckability[i])
					.map(t -> t.toString()).collect(Collectors.joining(", ")),
			IntStream.range(0, propertyInsertability.length).mapToObj(i -> propertyInsertability[i])
					.map(t -> t.toString()).collect(Collectors.joining(", ")),
			IntStream.range(0, propertyVersionability.length).mapToObj(i -> propertyVersionability[i])
					.map(t -> t.toString()).collect(Collectors.joining(", ")),
			hasCollections,
			isAbstract,
			optimisticLockStyle.toString(),
			polymorphic,
			superClass,
			inherited,
			hasSubclasses,
			subclassEntityNames.stream().collect(Collectors.joining(", ")),
			mode.toString(),
			tuplizer.getClass().getName()
		);
		// @formatter:on
	}

}
