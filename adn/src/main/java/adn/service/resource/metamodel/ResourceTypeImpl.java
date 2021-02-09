/**
 * 
 */
package adn.service.resource.metamodel;

import java.io.Serializable;

import javax.persistence.metamodel.EntityType;

import adn.service.resource.OptimisticLockStyle;

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

	private final OptimisticLockStyle optimisticLockStyle;

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
			boolean isVersioned,
			boolean isAbstract,
			OptimisticLockStyle optimisticLockStyle,
			boolean hasPojo) throws IllegalAccessException {
		super(javaType, superType, hasIdentifierProperty, hasIdClass, isVersioned, isAbstract, hasPojo);
		// @formatter:on
		// TODO Auto-generated constructor stub
		this.name = name;
		this.optimisticLockStyle = optimisticLockStyle;
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

	public OptimisticLockStyle getOptimisticLockStyle() {
		return optimisticLockStyle;
	}

}
