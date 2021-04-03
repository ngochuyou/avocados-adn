/**
 * 
 */
package adn.service.resource.metamodel;

import javax.persistence.metamodel.Type;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractType<X> implements Type<X> {

	private final Class<X> type;

	public AbstractType(Class<X> type) {
		// TODO Auto-generated constructor stub
		this.type = type;
	}

	@Override
	public Class<X> getJavaType() {
		// TODO Auto-generated method stub
		return type;
	}

}
