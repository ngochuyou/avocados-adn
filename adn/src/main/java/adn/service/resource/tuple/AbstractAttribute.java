/**
 * 
 */
package adn.service.resource.tuple;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractAttribute<X, Y> implements Attribute<X, Y> {

	private final String attributeName;

	private final Type<Y> attributeType;

	private final ManagedType<X> declaringType;

	private final boolean isNullable;

	private final boolean isUpdatable;

	private final boolean isInsertable;

	private final boolean isVersionable;

	private final PersistentAttributeType attributePersistentType;

	// @formatter:off
	public AbstractAttribute(
			String attributeName,
			Type<Y> attributeType,
			ManagedType<X> declaringType,
			boolean nullable,
			boolean updatable,
			boolean insertable,
			boolean versionable) {
		super();
		this.attributeName = attributeName;
		this.attributeType = attributeType;
		this.declaringType = declaringType;
		this.isNullable = nullable;
		this.isUpdatable = updatable;
		this.isInsertable = insertable;
		this.isVersionable = versionable;
		this.attributePersistentType = PersistentAttributeType.BASIC;
	}
	// @formatter:on
	public String getAttributeName() {
		return attributeName;
	}

	public Type<Y> getAttributeType() {
		return attributeType;
	}

	public ManagedType<X> getDeclaringType() {
		return declaringType;
	}

	public boolean isNullable() {
		return isNullable;
	}

	public boolean isUpdatable() {
		return isUpdatable;
	}

	public boolean isInsertable() {
		return isInsertable;
	}

	public boolean isVersionable() {
		return isVersionable;
	}

	public PersistentAttributeType getAttributePersistentType() {
		return attributePersistentType;
	}

}
