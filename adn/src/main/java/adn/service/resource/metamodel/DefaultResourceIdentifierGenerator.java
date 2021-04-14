/**
 * 
 */
package adn.service.resource.metamodel;

import java.io.Serializable;
import java.util.Date;
import java.util.Properties;

import org.hibernate.MappingException;
import org.hibernate.id.Configurable;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import adn.helpers.StringHelper;
import adn.service.resource.local.ResourceIdentifierGenerator;
import adn.service.resource.local.ResourceManagerFactory;
import adn.service.resource.models.Resource;

/**
 * @author Ngoc Huy
 *
 */
public class DefaultResourceIdentifierGenerator implements ResourceIdentifierGenerator<Serializable>, Configurable {

	public static final DefaultResourceIdentifierGenerator INSTANCE = new DefaultResourceIdentifierGenerator();

	public static final String IDENTIFIER_PARTS_SEPERATOR = "_";

	@Override
	public Serializable generate(ResourceManagerFactory factory, Object object) {
		// TODO Auto-generated method stub
		if (object instanceof Resource) {
			// @formatter:off
			Resource instance = (Resource) object;

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

	@Override
	public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
		// TODO Auto-generated method stub
	}

}
