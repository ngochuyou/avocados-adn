/**
 * 
 */
package adn.service.resource.engine;

import java.io.File;

import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.template.ResourceTemplate;
import adn.service.resource.engine.tuple.InstantiatorFactory.PojoInstantiator;

/**
 * @author Ngoc Huy
 *
 */
public interface Storage {
	// @formatter:off
	void registerTemplate(
			String templateName,
			String directoryName,
			String[] columnNames,
			Class<?>[] columnTypes,
			boolean[] columnNullabilities,
			PropertyAccessImplementor[] accessors,
			PojoInstantiator<File> instantiator) throws IllegalArgumentException;
	// @formatter:on
	ResultSetImplementor query(Query query);

	ResultSetImplementor execute(Query query);

	ResourceTemplate getResourceTemplate(String templateName);

	String getDirectory();

}
