/**
 * 
 */
package adn.service.resource.metamodel;

import java.io.Serializable;

import javax.persistence.metamodel.MappedSuperclassType;

/**
 * @author Ngoc Huy
 *
 */
public class MappedSuperClassImpl<X> extends AbstractIdentifiableType<X>
		implements MappedSuperclassType<X>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param javaType
	 * @param superType
	 * @param hasIdentifierProperty
	 * @param hasIdClass
	 * @param isVersioned
	 * @throws IllegalAccessException
	 */
	public MappedSuperClassImpl(Class<X> javaType, AbstractIdentifiableType<X> superType, boolean hasIdentifierProperty,
			boolean hasIdClass, boolean isVersioned, boolean isAbstract, boolean hasPojo)
			throws IllegalAccessException {
		super(javaType, superType, hasIdentifierProperty, hasIdClass, isVersioned, isAbstract, hasPojo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public PersistenceType getPersistenceType() {
		// TODO Auto-generated method stub
		return PersistenceType.MAPPED_SUPERCLASS;
	}

}
