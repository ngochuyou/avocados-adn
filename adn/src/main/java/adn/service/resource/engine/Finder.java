/**
 * 
 */
package adn.service.resource.engine;

import java.io.File;

import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.template.ResourceTemplate;

/**
 * Defines resource search contract
 * 
 * @author Ngoc Huy
 *
 */
public interface Finder {

	/**
	 * Directly find a {@link File} by the given path
	 * </p>
	 * 
	 * This method doesn't check for extension presence
	 * 
	 * @param path
	 * @return
	 */
	File find(String path);

	/**
	 * Directly find a {@link File} by the given path
	 * </p>
	 * 
	 * This method doesn't check for extension presence. Append root path if given
	 * path doesn't start with root path and <code>appendRoot == true</code>
	 */
	File find(String path, boolean appendRoot);

	/**
	 * Get all {@link File}(s) from the configured directory which satisfy a
	 * specific {@link Query}
	 * </p>
	 */
	File[] find(ResourceTemplate template, Object[] values);

	/**
	 * Get all {@link File}(s) from the configured directory which satisfy specific
	 * columns defined in {@link Query}
	 * </p>
	 */
	File[] find(ResourceTemplate template, Object[] values, String[] propertyNames);

	/**
	 * Check existence of a {@link File}
	 */
	boolean doesExist(File file);

	/**
	 * Check existence of a {@link File} with it's path
	 */
	boolean doesExist(String path);

}
