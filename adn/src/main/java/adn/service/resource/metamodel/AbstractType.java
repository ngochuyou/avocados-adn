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
	private static final long serialVersionUID = 1L;
	
	private final Class<X> javaType;

	public AbstractType(Class<X> javaType) {
		super();
		this.javaType = javaType;
	}

	@Override
	public Class<X> getJavaType() {
		// TODO Auto-generated method stub
		return javaType;
	}

}
