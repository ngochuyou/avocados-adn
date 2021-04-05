/**
 * 
 */
package adn.service.resource.metamodel;

import static adn.service.resource.local.ResourceManagerFactory.unsupport;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.Session;

import adn.service.resource.local.ResourceManagerFactory;
import adn.service.resource.local.ResourcePropertyValueGenerator;
import adn.service.resource.models.NamedResource;
import adn.utilities.StringHelper;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceIdentifierValueGenerator implements ResourcePropertyValueGenerator<Serializable> {

	public static final ResourceIdentifierValueGenerator INSTANCE = new ResourceIdentifierValueGenerator();

	public static final String IDENTIFIER_PARTS_SEPERATOR = "_";

	@Override
	@Deprecated
	public Serializable generateValue(Session session, Object owner) {
		// TODO Auto-generated method stub
		unsupport();
		return null;
	}

	@Override
	public Serializable generateValue(ResourceManagerFactory factory, Object object) {
		// TODO Auto-generated method stub
		if (object instanceof NamedResource) {
			// @formatter:off
			NamedResource instance = (NamedResource) object;

			return new StringBuilder(instance.getDirectoryPath())
					.append(new Date().getTime())
					.append(IDENTIFIER_PARTS_SEPERATOR)
					.append(StringHelper.hash(instance.getName()))
					.append(instance.getExtension())
					.toString();
			// @formatter:on
		}

		return String.valueOf(new Date().getTime());
	}

}
