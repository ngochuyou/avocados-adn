/**
 * 
 */
package adn.service.resource.metamodel;

import java.util.Set;

import org.hibernate.service.Service;

/**
 * @author Ngoc Huy
 *
 */
public interface Metadata extends Service {

	<X> ResourceClass<X> getResourceClass(String name);

	Set<String> getImports();

	public void process();

}
