/**
 * 
 */
package adn.service.resource.local;

import java.util.Set;

/**
 * @author Ngoc Huy
 *
 */
public interface Metadata extends Service {

	Set<Class<?>> getModelClassSet();
	
}
