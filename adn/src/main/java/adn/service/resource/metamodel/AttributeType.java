/**
 * 
 */
package adn.service.resource.metamodel;

/**
 * @author Ngoc Huy
 *
 */
public class AttributeType<X> extends AbstractType<X> {

	/**
	 * @param type
	 */
	public AttributeType(Class<X> type) {
		super(type);
		// TODO Auto-generated constructor stub
	}

	@Override
	public PersistenceType getPersistenceType() {
		// TODO Auto-generated method stub
		return PersistenceType.BASIC;
	}

}
