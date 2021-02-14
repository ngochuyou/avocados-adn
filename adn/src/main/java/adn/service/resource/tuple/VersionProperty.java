/**
 * 
 */
package adn.service.resource.tuple;

import java.lang.reflect.Member;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type;

/**
 * @author Ngoc Huy
 *
 */
public class VersionProperty<X, Y> extends AbstractAttribute<X, Y> implements Attribute<X, Y> {

	private final VersionValue unsavedValue;

	private final PersistentAttributeType persistentType;

	/**
	 * @param attributeName
	 * @param attributeType
	 * @param declaringType
	 * @param nullable
	 * @param updatable
	 * @param insertable
	 * @param versionable
	 */
	public VersionProperty(String attributeName, Type<Y> attributeType, ManagedType<X> declaringType,
			VersionValue value, PersistentAttributeType persistentType) {
		super(attributeName, attributeType, declaringType, false, true, true, true);
		// TODO Auto-generated constructor stub
		this.unsavedValue = value;
		this.persistentType = persistentType;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return super.getAttributeName();
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		// TODO Auto-generated method stub
		return persistentType;
	}

	@Override
	public Class<Y> getJavaType() {
		// TODO Auto-generated method stub
		return super.getAttributeType().getJavaType();
	}

	@Override
	public Member getJavaMember() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAssociation() {
		// TODO Auto-generated method stub
		return !(persistentType == PersistentAttributeType.BASIC || persistentType == PersistentAttributeType.EMBEDDED);
	}

	@Override
	public boolean isCollection() {
		// TODO Auto-generated method stub
		return persistentType == PersistentAttributeType.ELEMENT_COLLECTION;
	}

	public VersionValue getUnsavedValue() {
		return unsavedValue;
	}

}
