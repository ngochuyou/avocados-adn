/**
 * 
 */
package adn.service.resource.metamodel;

import java.io.Serializable;

import javax.persistence.metamodel.EmbeddableType;

/**
 * @author Ngoc Huy
 *
 */
public class EmbeddableTypeImpl<X> extends AbstractManagedType<X> implements EmbeddableType<X>, Serializable {

	private final AbstractManagedType<?> parent;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param javaType
	 * @param superType
	 */
	public EmbeddableTypeImpl(Class<X> javaType, AbstractManagedType<X> superType, AbstractManagedType<?> parent, boolean hasPojo) {
		super(javaType, superType, false, hasPojo);
		// TODO Auto-generated constructor stub
		this.parent = parent;
	}

	@Override
	public PersistenceType getPersistenceType() {
		// TODO Auto-generated method stub
		return PersistenceType.EMBEDDABLE;
	}

	public AbstractManagedType<?> getParent() {
		return parent;
	}

}
