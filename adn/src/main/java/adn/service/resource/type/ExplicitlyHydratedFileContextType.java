/**
 * 
 */
package adn.service.resource.type;

import org.hibernate.HibernateException;
import org.hibernate.type.BinaryType;

import adn.service.resource.model.hydrate.FileBytesHydrateFunction;
import adn.service.resource.model.models.Resource;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class ExplicitlyHydratedFileContextType extends ExplicitlyHydratedType<byte[], HibernateException> {

	public static final String NAME = "adn.service.resource.metamodel.type.ExplicitlyHydratedFileContextType";

	public ExplicitlyHydratedFileContextType() {
		super(BinaryType.INSTANCE, byte[].class, FileBytesHydrateFunction.INSTANCE);
	}

	@Override
	public String getAttributeName() {
		return Resource.RESOURCE_CONTENT_ATTRIBUTE_NAME;
	}

}
