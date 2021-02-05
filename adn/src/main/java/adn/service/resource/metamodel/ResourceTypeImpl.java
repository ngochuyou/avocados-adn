/**
 * 
 */
package adn.service.resource.metamodel;

import java.io.Serializable;

import javax.persistence.metamodel.EntityType;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceTypeImpl<X> extends AbstractIdentifiableType<X> implements EntityType<X>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String name;

	/**
	 * @param javaType
	 * @param superType
	 * @param hasIdentifierProperty
	 * @param hasIdClass
	 * @param isVersioned
	 * @throws IllegalAccessException
	 */
	// @formatter:off
	public ResourceTypeImpl(
			Class<X> javaType,
			String name,
			AbstractIdentifiableType<X> superType,
			boolean hasIdentifierProperty,
			boolean hasIdClass,
			boolean isVersioned) throws IllegalAccessException {
		super(javaType, superType, hasIdentifierProperty, hasIdClass, isVersioned);
		// @formatter:on
		// TODO Auto-generated constructor stub
		this.name = name;
	}

	@Override
	public PersistenceType getPersistenceType() {
		// TODO Auto-generated method stub
		return PersistenceType.ENTITY;
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

}
