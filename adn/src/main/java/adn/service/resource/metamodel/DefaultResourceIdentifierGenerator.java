/**
 * 
 */
package adn.service.resource.metamodel;

import java.io.Serializable;
import java.util.Date;

import adn.service.resource.local.ResourceIdentifierGenerator;
import adn.service.resource.local.ResourceManagerFactory;
import adn.service.resource.models.NamedResource;
import adn.utilities.StringHelper;

/**
 * @author Ngoc Huy
 *
 */
public class DefaultResourceIdentifierGenerator implements ResourceIdentifierGenerator<Serializable> {

	public static final DefaultResourceIdentifierGenerator INSTANCE = new DefaultResourceIdentifierGenerator();

	public static final String IDENTIFIER_PARTS_SEPERATOR = "_";

	@Override
	public Serializable generate(ResourceManagerFactory factory, Object object) {
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
