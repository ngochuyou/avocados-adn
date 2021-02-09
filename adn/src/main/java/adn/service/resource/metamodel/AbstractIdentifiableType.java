/**
 * 
 */
package adn.service.resource.metamodel;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractIdentifiableType<X> extends AbstractManagedType<X>
		implements IdentifiableType<X>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	private final boolean hasIdentifierProperty;
	private final boolean hasIdClass;
	private SingularAttribute<X, ?> identifier;
	private Set<SingularAttribute<? super X, ?>> idClassAttributes;

	private final boolean isVersioned;
	private SingularAttribute<X, ?> version;

	private transient AbstractIdentifiableType.Access<X> access;

	/**
	 * @param javaType
	 * @param superType
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AbstractIdentifiableType(Class<X> javaType, AbstractIdentifiableType<X> superType,
			boolean hasIdentifierProperty, boolean hasIdClass, boolean isVersioned, boolean isAbstract, boolean hasPojo)
			throws IllegalAccessException {
		super(javaType, superType, isAbstract, hasPojo);
		// TODO Auto-generated constructor stub
		this.hasIdentifierProperty = hasIdentifierProperty;
		this.hasIdClass = hasIdClass;
		this.isVersioned = isVersioned;

		this.access = new AbstractIdentifiableType.AccessImpl(superType != null ? superType.getAccess() : null);
	}

	public AbstractIdentifiableType.Access<X> getAccess() throws IllegalAccessException {
		if (this.access == null) {
			throw new IllegalAccessException(
					"Access to identifiable type of java type " + getJavaType() + " was blocked");
		}

		return this.access;
	}

	private SingularAttribute<? super X, ?> locateId() {
		if (identifier != null) {
			return identifier;
		}

		return getSupertype().locateId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <Y> SingularAttribute<? super X, Y> getId(Class<Y> type) {
		// TODO Auto-generated method stub
		if (hasIdClass) {
			return null;
		}

		SingularAttribute<? super X, ?> id = locateId();

		return isCorrectType(id, type) ? (SingularAttribute<? super X, Y>) id : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <Y> SingularAttribute<X, Y> getDeclaredId(Class<Y> type) {
		// TODO Auto-generated method stub
		if (hasIdClass) {
			return null;
		}

		return isCorrectType(identifier, type) ? (SingularAttribute<X, Y>) identifier : null;
	}

	private <T> boolean isCorrectType(@SuppressWarnings("rawtypes") SingularAttribute attr, Class<T> type) {

		return attr != null && type.isAssignableFrom(attr.getJavaType());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <Y> SingularAttribute<? super X, Y> getVersion(Class<Y> type) {
		// TODO Auto-generated method stub
		if (!isVersioned) {
			return null;
		}

		SingularAttribute<? super X, ?> attribute = locateVersion();

		if (attribute != null) {
			return isCorrectType(attribute, type) ? (SingularAttribute<? super X, Y>) attribute : null;
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public <Y> SingularAttribute<? super X, Y> getVersion() {

		return (SingularAttribute<? super X, Y>) locateVersion();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <Y> SingularAttribute<X, Y> getDeclaredVersion(Class<Y> type) {
		// TODO Auto-generated method stub
		return isCorrectType(version, type) ? (SingularAttribute<X, Y>) version : null;
	}

	private SingularAttribute<? super X, ?> locateVersion() {
		if (version != null) {
			return version;
		}

		if (getSupertype() != null) {
			return getSupertype().locateVersion();
		}

		return null;
	}

	@Override
	public AbstractIdentifiableType<? super X> getSupertype() {
		// TODO Auto-generated method stub
		return (AbstractIdentifiableType<? super X>) super.getSuperType();
	}

	@Override
	public boolean hasSingleIdAttribute() {
		// TODO Auto-generated method stub
		return !hasIdClass && hasIdentifierProperty;
	}

	@Override
	public boolean hasVersionAttribute() {
		// TODO Auto-generated method stub
		return isVersioned;
	}

	@Override
	public Set<SingularAttribute<? super X, ?>> getIdClassAttributes() {
		// TODO Auto-generated method stub
		if (!hasIdClass) {
			return null;
		}

		final Set<SingularAttribute<? super X, ?>> attributes = new HashSet<>();

		collectIdClassAttributes(attributes);

		if (attributes.isEmpty()) {
			throw new IllegalStateException("Unnable to collect @IdClass attributes");
		}

		return attributes;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void collectIdClassAttributes(Set<SingularAttribute<? super X, ?>> attributes) {
		if (idClassAttributes != null) {
			if (idClassAttributes == Collections.EMPTY_SET) {
				idClassAttributes = new HashSet<>();
			}

			attributes.addAll(idClassAttributes);

			return;
		}

		if (getSupertype() != null) {
			getSupertype().collectIdClassAttributes((Set) attributes);
		}
	}

	@Override
	public Type<?> getIdType() {
		// TODO Auto-generated method stub
		SingularAttribute<?, ?> id = locateId();

		if (id != null) {
			return id.getType();
		}

		Set<SingularAttribute<? super X, ?>> idClassAttributes = new HashSet<>();

		collectIdClassAttributes(idClassAttributes);

		if (idClassAttributes.size() == 1) {
			return idClassAttributes.iterator().next().getType();
		}

		return null;
	}

	interface Access<X> extends AbstractManagedType.Access<X> {

		void setIdAttribute(SingularAttribute<X, ?> idAttribute);

		void addIdClassAttributes(Set<SingularAttribute<? super X, ?>> idClassAttributes);

		void setVersionAttribute(SingularAttribute<X, ?> versionAttribute);

	}

	private class AccessImpl implements Access<X> {

		private AbstractManagedType.Access<X> managedTypeAccess;

		private AccessImpl(AbstractManagedType.Access<X> managedTypeAccess) {
			this.managedTypeAccess = managedTypeAccess;
		}

		@Override
		public void addAttribute(Attribute<X, ?> attribute) throws IllegalArgumentException {
			// TODO Auto-generated method stub
			managedTypeAccess.addAttribute(attribute);
		}

		@Override
		public void close() {
			// TODO Auto-generated method stub
			managedTypeAccess.close();
		}

		@Override
		public void setIdAttribute(SingularAttribute<X, ?> idAttribute) {
			// TODO Auto-generated method stub
			identifier = idAttribute;
			managedTypeAccess.addAttribute(idAttribute);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void addIdClassAttributes(Set<SingularAttribute<? super X, ?>> idClassAttributes) {
			// TODO Auto-generated method stub
			if (idClassAttributes.isEmpty()) {
				AbstractIdentifiableType.this.idClassAttributes = Collections.emptySet();

				return;
			}
			// @formatter:off
			AbstractIdentifiableType.this.idClassAttributes = idClassAttributes
					.stream()
					.filter(attr -> AbstractIdentifiableType.this == attr.getDeclaringType())
					.map(attr -> {
						addAttribute((SingularAttribute<X, ?>) attr);
						return attr;
					}).collect(Collectors.toSet());
			// @formatter:on
		}

		@Override
		public void setVersionAttribute(SingularAttribute<X, ?> versionAttribute) {
			// TODO Auto-generated method stub
			version = versionAttribute;
			managedTypeAccess.addAttribute(versionAttribute);
		}

	}

}
