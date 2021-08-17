/**
 * 
 */
package adn.controller.query;

import java.util.Collection;

/**
 * @author Ngoc Huy
 *
 */
public interface ColumnsRequest extends RestQuery {

	/**
	 * @return every requested columns as a {@code Collection}
	 */
	Collection<String> join();

}
