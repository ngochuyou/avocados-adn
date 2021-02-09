/**
 * 
 */
package adn.service.resource.metamodel;

import java.io.Serializable;

import javax.persistence.metamodel.Type;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractType<X> implements Type<X>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	private final Class<X> javaType;

	private final boolean isAbstract;

	public AbstractType(Class<X> javaType, boolean isAbstract) {
		super();
		this.javaType = javaType;
		this.isAbstract = isAbstract;
	}

	@Override
	public Class<X> getJavaType() {
		// TODO Auto-generated method stub
		return javaType;
	}

	public boolean isAssociation() {
		return false;
	}

	public boolean isCollection() {
		return false;
	}

	public boolean isEntityType() {
		return false;
	}

	public boolean isMutable() {
		return false;
	}

	public String getName() {

		return javaType.getSimpleName();
	}

	public boolean isAbstract() {
		return isAbstract;
	}

}
