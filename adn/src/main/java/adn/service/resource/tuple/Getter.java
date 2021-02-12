/**
 * 
 */
package adn.service.resource.tuple;

import java.lang.reflect.Member;

/**
 * @author Ngoc Huy
 *
 */
public interface Getter {

	Object get(Object o) throws Exception;

	Class<?> getReturnType();

	Member getMember();

}
