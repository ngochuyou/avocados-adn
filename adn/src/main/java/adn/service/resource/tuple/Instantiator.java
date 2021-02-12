/**
 * 
 */
package adn.service.resource.tuple;

import java.io.Serializable;

/**
 * @author Ngoc Huy
 *
 */
public interface Instantiator {

	public Object instantiate(Serializable id) throws Exception;

	public boolean isInstance(Object object);

}
