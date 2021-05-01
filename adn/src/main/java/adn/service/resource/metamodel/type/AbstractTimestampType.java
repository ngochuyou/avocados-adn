/**
 * 
 */
package adn.service.resource.metamodel.type;

import org.hibernate.type.TimestampType;

/**
 * @author Ngoc Huy
 * 
 */
@SuppressWarnings("serial")
public abstract class AbstractTimestampType extends AbstractSynthesizedBasicType {

	protected AbstractTimestampType(TimestampType basicType) {
		super(basicType);
	}

}
