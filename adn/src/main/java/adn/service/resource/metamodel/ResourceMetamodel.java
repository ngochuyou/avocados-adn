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
import adn.service.resource.tuple.ResourceTuplizer;
import adn.utilities.TypeUtils;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceMetamodel {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final EntityManager resourceManager;

	private static final int NO_VERSION_INDEX = -666;

	private final String name;
	private final String rootName;

	private final IdentifierProperty<?, ?> identifierAttribute;
	private final boolean versioned;

	private final int propertySpan;
	private final int versionPropertyIndex;
	private final Attribute<?, ?>[] properties;

	private final String[] propertyNames;
	private final Type<?>[] propertyTypes;
	private final boolean[] propertyUpdateability;
	private final boolean[] propertyNullability;
	private final boolean[] propertyCheckability;
	private final boolean[] propertyInsertability;
	private final boolean[] propertyVersionability;

	private final Map<String, Integer> propertyIndexes = new HashMap<>();
	private final boolean hasCollections;
	private final BitSet mutablePropertiesIndexes;

	private final boolean isAbstract;
	private final OptimisticLockStyle optimisticLockStyle;

	private final boolean polymorphic;
	private final String superclass;
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
				id.getType(), id.getType() instanceof EmbeddableTypeImpl, IdentifierValue.NULL, null, type);

		Field[] fields = TypeUtils.getAllFields(type.getJavaType());

		properties = type.getAttributes().toArray(Attribute<?, ?>[]::new);

		visitAttributes(properties, p -> p instanceof AbstractAttribute);

		propertySpan = properties.length;
		versionPropertyIndex = getAttributeIndex(fields, type.getVersion().getName());

		propertyNames = Stream.of(properties).map(p -> p.getName()).toArray(String[]::new);
		propertyTypes = Stream.of(properties).map(p -> {
			return ((AbstractAttribute<X, ?>) p).getAttributeType();
		}).toArray(Type<?>[]::new);

		propertyUpdateability = new boolean[propertySpan];
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
			propertyUpdateability[i] = casted.isUpdatable();
			propertyNullability[i] = casted.isNullable();
			propertyInsertability[i] = casted.isInsertable();
			propertyVersionability[i] = casted.isVersionable();
			propertyIndexes.put(casted.getAttributeName(), i);
			foundCollections = foundCollections || Collection.class.isAssignableFrom(casted.getJavaType());

			if (((AbstractType<?>) ((AbstractAttribute<?, ?>) propertyTypes[i]).getAttributeType()).isMutable()
					&& propertyCheckability[i]) {
				mutableIndexes.set(i);
			}
		}

		hasCollections = foundCollections;
		mutablePropertiesIndexes = new BitSet(propertySpan);
		isAbstract = type.isAbstract();
		optimisticLockStyle = type.getOptimisticLockStyle();
		polymorphic = type.isPolymorphic();
		hasSubclasses = type.hasSubtypes();
		superclass = !inherited ? null : type.getSuperType().getName();
		explicitPolymorphism = true;
		subclassEntityNames = type.getSubtypes().stream().map(t -> t.getName()).collect(Collectors.toSet());

		mode = type.hasPojo() ? EntityMode.POJO : EntityMode.MAP;
		tuplizer = null;
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

	public boolean getVersioned() {
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
		return propertyUpdateability;
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
		return propertyIndexes;
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

	public boolean getIsAbstract() {
		return isAbstract;
	}

	public boolean getPolymorphic() {
		return polymorphic;
	}

	public String getSuperclass() {
		return superclass;
	}

	public boolean getExplicitPolymorphism() {
		return explicitPolymorphism;
	}

	public boolean getInherited() {
		return inherited;
	}

	public boolean getHasSubclasses() {
		return hasSubclasses;
	}

	public Set<String> getSubclassEntityNames() {
		return subclassEntityNames;
	}

}
