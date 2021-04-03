/**
 * 
 */
package adn.service.resource.metamodel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.Property;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.GetterMethodImpl;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.property.access.spi.SetterMethodImpl;
import org.hibernate.tuple.ValueGeneration;

import adn.service.resource.metamodel.MetamodelImpl.VersionValue;
import adn.utilities.StringHelper;

/**
 * @author Ngoc Huy
 *
 */
public class EntityTypeImpl<X> extends AbstractType<X> implements EntityType<X> {

	private final String name;
	private final Map<String, SingularAttribute<X, ?>> attributes;
	private final int propertySpan;
	private final Map<String, Integer> indexMap;
	private final ValueGeneration[] valueGenerations;
	private final Class<?>[] propertyTypes;

	private SingularAttribute<X, ?> identifier;
	private SingularAttribute<X, ?> version;
	private Getter idGetter;
	private Setter idSetter;
	private ValueGeneration identifierValueGeneration;

	private final Getter[] getters;
	private final Setter[] setters;
	private final boolean[] autoGeneratedMarkers;

	public EntityTypeImpl(Class<X> entityType, String entityName, ResourceClass typeClass)
			throws NoSuchMethodException, SecurityException, NoSuchFieldException {
		super(entityType);
		// TODO Auto-generated constructor stub
		this.name = entityName;
		attributes = new HashMap<>();
		propertySpan = typeClass.getProperties().size();
		propertyTypes = typeClass.getPropertyTypes().toArray(Class<?>[]::new);
		indexMap = new HashMap<>(propertySpan);
		valueGenerations = new ValueGeneration[propertySpan];
		getters = new Getter[propertySpan];
		setters = new Setter[propertySpan];
		autoGeneratedMarkers = new boolean[propertySpan];

		for (int i = 0; i < propertySpan; i++) {
			final Property prop = typeClass.getProperties().get(i);
			final Class<?> propType = typeClass.getPropertyTypes().get(i);
			final AttributeType<?> attrType = new AttributeType<>(propType);
			final Field field = entityType.getDeclaredField(prop.getName());
			final String propName = prop.getName();

			Method method = entityType.getDeclaredMethod(
					StringHelper.toCamel("get " + field.getName(), StringHelper.MULTIPLE_MATCHES_WHITESPACE_CHARS));
			final Getter getter = new GetterMethodImpl(entityType, field.getName(), method);

			method = entityType.getDeclaredMethod(
					StringHelper.toCamel("set " + field.getName(), StringHelper.MULTIPLE_MATCHES_WHITESPACE_CHARS),
					field.getType());
			getters[i] = getter;

			final Setter setter = new SetterMethodImpl(entityType, field.getName(), method);

			setters[i] = setter;

			indexMap.put(prop.getName(), i);
			valueGenerations[i] = prop.getValueGenerationStrategy();
			autoGeneratedMarkers[i] = valueGenerations[i] != null;

			attributes.compute(propName, (k, v) -> {
				if (prop.getValue() instanceof KeyValue) {
					identifier = new ResourceAttribute<>(propName, this, tryGetType(propType), true, false,
							prop.isOptional(), attrType);
					idGetter = getter;
					idSetter = setter;
					identifierValueGeneration = prop.getValueGenerationStrategy();

					return identifier;
				}

				if (prop.getValue() instanceof VersionValue) {
					version = new ResourceAttribute<>(propName, this, tryGetType(propType), false, true,
							prop.isOptional(), attrType);

					return version;
				}

				return new ResourceAttribute<>(propName, this, tryGetType(propType), false, false, prop.isOptional(),
						attrType);
			});
		}
	}

	@SuppressWarnings("unchecked")
	private <Y> Class<Y> tryGetType(Class<?> type) {
		return (Class<Y>) type;
	}

	@Override
	public PersistenceType getPersistenceType() {
		// TODO Auto-generated method stub
		return PersistenceType.ENTITY;
	}

	@Override
	public Set<Attribute<? super X, ?>> getAttributes() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableSet(new HashSet<>(attributes.values()));
	}

	@Override
	public Set<Attribute<X, ?>> getDeclaredAttributes() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableSet(new HashSet<>(attributes.values()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <Y> SingularAttribute<? super X, Y> getSingularAttribute(String name, Class<Y> type) {
		// TODO Auto-generated method stub
		return (SingularAttribute<? super X, Y>) attributes.get(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <Y> SingularAttribute<X, Y> getDeclaredSingularAttribute(String name, Class<Y> type) {
		// TODO Auto-generated method stub
		return (SingularAttribute<X, Y>) attributes.get(name);
	}

	@Override
	public Set<SingularAttribute<? super X, ?>> getSingularAttributes() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableSet(new HashSet<>(attributes.values()));
	}

	@Override
	public Set<SingularAttribute<X, ?>> getDeclaredSingularAttributes() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableSet(new HashSet<>(attributes.values()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> CollectionAttribute<? super X, E> getCollection(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return (CollectionAttribute<? super X, E>) Collections.emptySet();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> CollectionAttribute<X, E> getDeclaredCollection(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return (CollectionAttribute<X, E>) Collections.emptySet();
	}

	@Override
	public <E> SetAttribute<? super X, E> getSet(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> SetAttribute<X, E> getDeclaredSet(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> ListAttribute<? super X, E> getList(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E> ListAttribute<X, E> getDeclaredList(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <K, V> MapAttribute<? super X, K, V> getMap(String name, Class<K> keyType, Class<V> valueType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <K, V> MapAttribute<X, K, V> getDeclaredMap(String name, Class<K> keyType, Class<V> valueType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<PluralAttribute<? super X, ?, ?>> getPluralAttributes() {
		// TODO Auto-generated method stub
		return Collections.emptySet();
	}

	@Override
	public Set<PluralAttribute<X, ?, ?>> getDeclaredPluralAttributes() {
		// TODO Auto-generated method stub
		return Collections.emptySet();
	}

	@Override
	public Attribute<? super X, ?> getAttribute(String name) {
		// TODO Auto-generated method stub
		return attributes.get(name);
	}

	@Override
	public Attribute<X, ?> getDeclaredAttribute(String name) {
		// TODO Auto-generated method stub
		return attributes.get(name);
	}

	@Override
	public SingularAttribute<? super X, ?> getSingularAttribute(String name) {
		// TODO Auto-generated method stub
		return attributes.get(name);
	}

	@Override
	public SingularAttribute<X, ?> getDeclaredSingularAttribute(String name) {
		// TODO Auto-generated method stub
		return attributes.get(name);
	}

	@Override
	public CollectionAttribute<? super X, ?> getCollection(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CollectionAttribute<X, ?> getDeclaredCollection(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SetAttribute<? super X, ?> getSet(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SetAttribute<X, ?> getDeclaredSet(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListAttribute<? super X, ?> getList(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListAttribute<X, ?> getDeclaredList(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapAttribute<? super X, ?, ?> getMap(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapAttribute<X, ?, ?> getDeclaredMap(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <Y> SingularAttribute<? super X, Y> getId(Class<Y> type) {
		// TODO Auto-generated method stub
		return (SingularAttribute<? super X, Y>) identifier;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <Y> SingularAttribute<X, Y> getDeclaredId(Class<Y> type) {
		// TODO Auto-generated method stub
		return (SingularAttribute<X, Y>) identifier;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <Y> SingularAttribute<? super X, Y> getVersion(Class<Y> type) {
		// TODO Auto-generated method stub
		return (SingularAttribute<? super X, Y>) version;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <Y> SingularAttribute<X, Y> getDeclaredVersion(Class<Y> type) {
		// TODO Auto-generated method stub
		return (SingularAttribute<X, Y>) version;
	}

	@Override
	public IdentifiableType<? super X> getSupertype() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasSingleIdAttribute() {
		// TODO Auto-generated method stub
		return identifier instanceof SingularAttribute;
	}

	@Override
	public boolean hasVersionAttribute() {
		// TODO Auto-generated method stub
		return version != null;
	}

	@Override
	public Set<SingularAttribute<? super X, ?>> getIdClassAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type<?> getIdType() {
		// TODO Auto-generated method stub
		return identifier.getType();
	}

	@Override
	public BindableType getBindableType() {
		// TODO Auto-generated method stub
		return BindableType.ENTITY_TYPE;
	}

	@Override
	public Class<X> getBindableJavaType() {
		// TODO Auto-generated method stub
		return getJavaType();
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@SuppressWarnings("unchecked")
	public <E> Class<E> locatePropertyType(String propertyName) {
		return (Class<E>) getPropertyType(propertyName);
	}

	public Class<?> getPropertyType(String propertyName) {
		return propertyTypes[indexMap.get(propertyName)];
	}

	public Getter[] getGetters() {
		return getters;
	}

	public Setter[] getSetters() {
		return setters;
	}

	public Getter getGetter(String propertyName) {

		return getters[indexMap.get(propertyName)];
	}

	public Setter getSetter(String propertyName) {

		return setters[indexMap.get(propertyName)];
	}

	public Getter getIdGetter() {

		return idGetter;
	}

	public Setter getIdSetter() {

		return idSetter;
	}

	public ValueGeneration getIdValueGeneration() {
		return identifierValueGeneration;
	}

	public int getPropertySpan() {
		return propertySpan;
	}

	public boolean[] getAutoGeneratedMarkers() {
		return autoGeneratedMarkers;
	}

	public int getPropertyIndex(String propertyName) {
		return indexMap.get(propertyName);
	}

}
