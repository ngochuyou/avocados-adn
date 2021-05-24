/**
 * 
 */
package adn.service.resource.engine;

import java.sql.SQLException;

import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.template.ResourceTemplate;

/**
 * @author Ngoc Huy
 *
 */
public interface LocalStorage {

	void registerTemplate(ResourceTemplate template) throws IllegalArgumentException;

	ResultSetImplementor query(Query query) throws SQLException;

}
