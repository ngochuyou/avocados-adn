/**
 * 
 */
package adn.service.resource.metamodel;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;

/**
 * @author Ngoc Huy
 *
 */
public class AttributeFactory {

	private final MetadataContext context;

	/**
	 * 
	 */
	public AttributeFactory(MetadataContext context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}
	
	public <X, Y> Attribute<X, Y> buildAttribute(ManagedType<X> owner) {
		
		return null;
	}

}
