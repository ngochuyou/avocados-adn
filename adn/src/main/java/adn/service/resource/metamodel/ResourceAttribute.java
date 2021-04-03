/**
 * 
 */
package adn.service.resource.metamodel;

import java.lang.reflect.Member;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceAttribute<X, Y> implements SingularAttribute<X, Y> {

	private final String name;
	private final ManagedType<X> owner;
	private final Class<Y> type;
	private final boolean isId;
	private final boolean isVersion;
	private final boolean isOptional;
	private final Type<Y> attributeType;

	/**
	 * 
	 */
	public ResourceAttribute(String name, ManagedType<X> owner, Class<Y> type, boolean isId, boolean isVersion,
			boolean isOptional, Type<Y> attributeType) {
		// TODO Auto-generated constructor stub
		this.name = name;
		this.owner = owner;
		this.type = type;
		this.isId = isId;
		this.isVersion = isVersion;
		this.isOptional = isOptional;
		this.attributeType = attributeType;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		// TODO Auto-generated method stub
		return PersistentAttributeType.BASIC;
	}

	@Override
	public ManagedType<X> getDeclaringType() {
		// TODO Auto-generated method stub
		return owner;
	}

	@Override
	public Class<Y> getJavaType() {
		// TODO Auto-generated method stub
		return type;
	}

	@Override
	public Member getJavaMember() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAssociation() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCollection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BindableType getBindableType() {
		// TODO Auto-generated method stub
		return BindableType.SINGULAR_ATTRIBUTE;
	}

	@Override
	public Class<Y> getBindableJavaType() {
		// TODO Auto-generated method stub
		return type;
	}

	@Override
	public boolean isId() {
		// TODO Auto-generated method stub
		return isId;
	}

	@Override
	public boolean isVersion() {
		// TODO Auto-generated method stub
		return isVersion;
	}

	@Override
	public boolean isOptional() {
		// TODO Auto-generated method stub
		return isOptional;
	}

	@Override
	public Type<Y> getType() {
		// TODO Auto-generated method stub
		return attributeType;
	}

}
