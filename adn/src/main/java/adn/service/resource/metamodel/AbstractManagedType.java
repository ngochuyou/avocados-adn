/**
 * 
 */
package adn.service.resource.metamodel;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.Bindable.BindableType;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractManagedType<X> extends AbstractType<X> implements ManagedType<X>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	private final AbstractManagedType<? super X> superType;

	private final Map<String, Attribute<X, ?>> declaredAttributes = new HashMap<>();
	private final Map<String, SingularAttribute<X, ?>> declaredSingularAttributes = new HashMap<>();
	private final Map<String, PluralAttribute<X, ?, ?>> declaredPluralAttributes = new HashMap<>();

	private transient AbstractManagedType.Access<X> access;

	/**
	 * @param javaType
	 * @param superType
	 */
	public AbstractManagedType(Class<X> javaType, AbstractManagedType<X> superType) {
		super(javaType);
		// TODO Auto-generated constructor stub
		this.superType = superType;

		access = new AccessImpl();
	}

	public AbstractManagedType.Access<X> getAccess() throws IllegalAccessException {
		if (access == null) {
			throw new IllegalAccessException(
					"Access to managed type of java type " + getJavaType().getName() + " was blocked");
		}

		return access;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Attribute<? super X, ?>> getAttributes() {
		// TODO Auto-generated method stub
		HashSet<Attribute<? super X, ?>> result = new HashSet<>();

		result.addAll((Collection<? extends Attribute<? super X, ?>>) declaredAttributes);

		if (superType != null) {
			result.addAll(superType.getAttributes());
		}

		return result;
	}

	@Override
	public Set<Attribute<X, ?>> getDeclaredAttributes() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableSet((Set<Attribute<X, ?>>) declaredAttributes.values());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <Y> SingularAttribute<? super X, Y> getSingularAttribute(String name, Class<Y> type) {
		// TODO Auto-generated method stub
		SingularAttribute<? super X, ?> attr = declaredSingularAttributes.get(name);

		if (attr == null && superType != null) {
			attr = superType.getDeclaredSingularAttribute(name, type);
		}

		return isCorrectType(attr, type) ? (SingularAttribute<? super X, Y>) attr : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <Y> SingularAttribute<X, Y> getDeclaredSingularAttribute(String name, Class<Y> type) {
		// TODO Auto-generated method stub
		SingularAttribute<X, ?> attr = declaredSingularAttributes.get(name);

		return attr == null ? null : isCorrectType(attr, type) ? (SingularAttribute<X, Y>) attr : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<SingularAttribute<? super X, ?>> getSingularAttributes() {
		// TODO Auto-generated method stub
		HashSet<SingularAttribute<? super X, ?>> result = new HashSet<>();

		result.addAll((Collection<? extends SingularAttribute<? super X, ?>>) declaredSingularAttributes);

		if (superType != null) {
			result.addAll(superType.getSingularAttributes());
		}

		return result;
	}

	@Override
	public Set<SingularAttribute<X, ?>> getDeclaredSingularAttributes() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableSet((Set<SingularAttribute<X, ?>>) declaredSingularAttributes.values());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> CollectionAttribute<? super X, E> getCollection(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		PluralAttribute<? super X, ?, ?> attr = declaredPluralAttributes.get(name);

		if (attr == null && superType != null) {
			attr = superType.getCollection(name, elementType);
		}

		return isCollection(attr) ? (CollectionAttribute<? super X, E>) attr : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> CollectionAttribute<X, E> getDeclaredCollection(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		PluralAttribute<? super X, ?, ?> attr = declaredPluralAttributes.get(name);

		return isCollection(attr) ? (CollectionAttribute<X, E>) attr : null;
	}

	private boolean isCollection(PluralAttribute<? super X, ?, ?> pluralAttr) {

		return pluralAttr != null && CollectionAttribute.class.isAssignableFrom(pluralAttr.getClass());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> SetAttribute<? super X, E> getSet(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		PluralAttribute<? super X, ?, ?> attr = declaredPluralAttributes.get(name);

		if (attr == null && superType != null) {
			attr = superType.getSet(name, elementType);
		}

		return isSet(attr) ? (SetAttribute<? super X, E>) attr : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> SetAttribute<X, E> getDeclaredSet(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		PluralAttribute<? super X, ?, ?> attr = declaredPluralAttributes.get(name);

		return isSet(attr) ? (SetAttribute<X, E>) attr : null;
	}

	private boolean isSet(PluralAttribute<? super X, ?, ?> pluralAttr) {

		return pluralAttr != null && SetAttribute.class.isAssignableFrom(pluralAttr.getClass());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> ListAttribute<? super X, E> getList(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		PluralAttribute<? super X, ?, ?> attr = declaredPluralAttributes.get(name);

		if (attr == null && superType != null) {
			attr = superType.getList(name, elementType);
		}

		return isList(attr) ? (ListAttribute<? super X, E>) attr : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> ListAttribute<X, E> getDeclaredList(String name, Class<E> elementType) {
		// TODO Auto-generated method stub
		PluralAttribute<? super X, ?, ?> attr = declaredPluralAttributes.get(name);

		return isList(attr) ? (ListAttribute<X, E>) attr : null;
	}

	private boolean isList(PluralAttribute<? super X, ?, ?> pluralAttr) {

		return pluralAttr != null && ListAttribute.class.isAssignableFrom(pluralAttr.getClass());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, V> MapAttribute<? super X, K, V> getMap(String name, Class<K> keyType, Class<V> valueType) {
		// TODO Auto-generated method stub
		PluralAttribute<? super X, ?, ?> attr = declaredPluralAttributes.get(name);

		if (attr == null && superType != null) {
			attr = superType.getMap(name, keyType, valueType);
		}

		return isMap(attr) ? (MapAttribute<? super X, K, V>) attr : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K, V> MapAttribute<X, K, V> getDeclaredMap(String name, Class<K> keyType, Class<V> valueType) {
		// TODO Auto-generated method stub
		PluralAttribute<? super X, ?, ?> attr = declaredPluralAttributes.get(name);

		return isMap(attr) ? (MapAttribute<X, K, V>) attr : null;
	}

	private boolean isMap(PluralAttribute<? super X, ?, ?> pluralAttr) {

		return pluralAttr != null && MapAttribute.class.isAssignableFrom(pluralAttr.getClass());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<PluralAttribute<? super X, ?, ?>> getPluralAttributes() {
		// TODO Auto-generated method stub
		HashSet<PluralAttribute<? super X, ?, ?>> result = new HashSet<>();

		result.addAll((Collection<? extends PluralAttribute<? super X, ?, ?>>) declaredPluralAttributes);

		if (superType != null) {
			result.addAll(superType.getPluralAttributes());
		}

		return result;
	}

	@Override
	public Set<PluralAttribute<X, ?, ?>> getDeclaredPluralAttributes() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableSet((Set<PluralAttribute<X, ?, ?>>) declaredPluralAttributes.values());
	}

	@Override
	public Attribute<? super X, ?> getAttribute(String name) {
		// TODO Auto-generated method stub
		Attribute<? super X, ?> attr = declaredAttributes.get(name);

		if (attr == null && superType != null) {
			attr = superType.getAttribute(name);
		}

		return attr;
	}

	@Override
	public Attribute<X, ?> getDeclaredAttribute(String name) {
		// TODO Auto-generated method stub
		return declaredAttributes.get(name);
	}

	@Override
	public SingularAttribute<? super X, ?> getSingularAttribute(String name) {
		// TODO Auto-generated method stub
		SingularAttribute<? super X, ?> attr = declaredSingularAttributes.get(name);

		if (attr == null && superType != null) {
			attr = superType.getSingularAttribute(name);
		}

		return attr;
	}

	@Override
	public SingularAttribute<X, ?> getDeclaredSingularAttribute(String name) {
		// TODO Auto-generated method stub
		return declaredSingularAttributes.get(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public CollectionAttribute<? super X, ?> getCollection(String name) {
		// TODO Auto-generated method stub
		PluralAttribute<? super X, ?, ?> attr = declaredPluralAttributes.get(name);

		if (attr == null && superType != null) {
			attr = superType.getCollection(name);
		}

		return isCollection(attr) ? (CollectionAttribute<? super X, ?>) attr : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CollectionAttribute<X, ?> getDeclaredCollection(String name) {
		// TODO Auto-generated method stub
		PluralAttribute<X, ?, ?> attr = declaredPluralAttributes.get(name);

		return isCollection(attr) ? (CollectionAttribute<X, ?>) attr : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SetAttribute<? super X, ?> getSet(String name) {
		// TODO Auto-generated method stub
		PluralAttribute<? super X, ?, ?> attr = declaredPluralAttributes.get(name);

		if (attr == null && superType != null) {
			attr = superType.getSet(name);
		}

		return isSet(attr) ? (SetAttribute<? super X, ?>) attr : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SetAttribute<X, ?> getDeclaredSet(String name) {
		// TODO Auto-generated method stub
		PluralAttribute<X, ?, ?> attr = declaredPluralAttributes.get(name);

		return isSet(attr) ? (SetAttribute<X, ?>) attr : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ListAttribute<? super X, ?> getList(String name) {
		// TODO Auto-generated method stub
		PluralAttribute<? super X, ?, ?> attr = declaredPluralAttributes.get(name);

		if (attr == null && superType != null) {
			attr = superType.getList(name);
		}

		return isList(attr) ? (ListAttribute<? super X, ?>) attr : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ListAttribute<X, ?> getDeclaredList(String name) {
		// TODO Auto-generated method stub
		PluralAttribute<X, ?, ?> attr = declaredPluralAttributes.get(name);

		return isList(attr) ? (ListAttribute<X, ?>) attr : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public MapAttribute<? super X, ?, ?> getMap(String name) {
		// TODO Auto-generated method stub
		PluralAttribute<? super X, ?, ?> attr = declaredPluralAttributes.get(name);

		if (attr == null && superType != null) {
			attr = superType.getMap(name);
		}

		return isMap(attr) ? (MapAttribute<? super X, ?, ?>) attr : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public MapAttribute<X, ?, ?> getDeclaredMap(String name) {
		// TODO Auto-generated method stub
		PluralAttribute<X, ?, ?> attr = declaredPluralAttributes.get(name);

		return isMap(attr) ? (MapAttribute<X, ?, ?>) attr : null;
	}

	private <Y> boolean isCorrectType(@SuppressWarnings("rawtypes") SingularAttribute attribute, Class<Y> javaType) {
		if (attribute == null || (javaType != null && !attribute.getBindableJavaType().equals(javaType))) {
			if (isPrimitiveVariant(attribute, javaType)) {
				return true;
			}

			return false;
		}

		return true;
	}

	@SuppressWarnings({ "rawtypes" })
	protected <Y> boolean isPrimitiveVariant(SingularAttribute<?, ?> attribute, Class<Y> javaType) {
		if (attribute == null) {
			return false;
		}

		Class declaredType = attribute.getBindableJavaType();

		if (declaredType.isPrimitive()) {
			return (Boolean.class.equals(javaType) && Boolean.TYPE.equals(declaredType))
					|| (Character.class.equals(javaType) && Character.TYPE.equals(declaredType))
					|| (Byte.class.equals(javaType) && Byte.TYPE.equals(declaredType))
					|| (Short.class.equals(javaType) && Short.TYPE.equals(declaredType))
					|| (Integer.class.equals(javaType) && Integer.TYPE.equals(declaredType))
					|| (Long.class.equals(javaType) && Long.TYPE.equals(declaredType))
					|| (Float.class.equals(javaType) && Float.TYPE.equals(declaredType))
					|| (Double.class.equals(javaType) && Double.TYPE.equals(declaredType));
		}

		if (javaType.isPrimitive()) {
			return (Boolean.class.equals(declaredType) && Boolean.TYPE.equals(javaType))
					|| (Character.class.equals(declaredType) && Character.TYPE.equals(javaType))
					|| (Byte.class.equals(declaredType) && Byte.TYPE.equals(javaType))
					|| (Short.class.equals(declaredType) && Short.TYPE.equals(javaType))
					|| (Integer.class.equals(declaredType) && Integer.TYPE.equals(javaType))
					|| (Long.class.equals(declaredType) && Long.TYPE.equals(javaType))
					|| (Float.class.equals(declaredType) && Float.TYPE.equals(javaType))
					|| (Double.class.equals(declaredType) && Double.TYPE.equals(javaType));
		}

		return false;
	}

	public AbstractManagedType<? super X> getSuperType() {
		return superType;
	}

	protected interface Access<X> {

		void addAttribute(Attribute<X, ?> attribute) throws IllegalArgumentException;

		void close();

	}

	protected class AccessImpl implements AbstractManagedType.Access<X> {

		@SuppressWarnings("unchecked")
		@Override
		public void addAttribute(Attribute<X, ?> attribute) throws IllegalArgumentException {
			// TODO Auto-generated method stub
			declaredAttributes.put(attribute.getName(), attribute);

			if (attribute instanceof Bindable) {
				BindableType bindableType = ((Bindable<X>) attribute).getBindableType();

				switch (bindableType) {
				case SINGULAR_ATTRIBUTE:
					declaredSingularAttributes.put(attribute.getName(), (SingularAttribute<X, ?>) attribute);

					break;
				case PLURAL_ATTRIBUTE:
					declaredPluralAttributes.put(attribute.getName(), (PluralAttribute<X, ?, ?>) attribute);

					break;
				default:
					throw new IllegalArgumentException("Unknown BindableType");
				}
			}
		}

		@Override
		public void close() {
			// TODO Auto-generated method stub
			access = null;
		}

	}

}
