/**
 * 
 */
package adn.service.resource.metamodel.type;

import org.hibernate.HibernateException;
import org.hibernate.type.BinaryType;

import adn.service.resource.model.hydrate.ByteArrayHydrateFunction;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class ExplicitlyHydratedFileContextType extends ExplicitlyHydratedType<byte[], HibernateException> {

	public static final String NAME = "adn.service.resource.metamodel.type.ExplicitlyHydratedFileContextType";

	public ExplicitlyHydratedFileContextType() {
		super(BinaryType.INSTANCE, byte[].class, ByteArrayHydrateFunction.INSTANCE);
	}

}
