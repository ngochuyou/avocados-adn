/**
 * 
 */
package adn.service.resource.local;

import java.util.Set;

import org.hibernate.service.Service;

/**
 * @author Ngoc Huy
 *
 */
public interface Metadata extends Service {

	Set<Class<?>> getModelClassSet();

}
