/**
 * 
 */
package adn.service.resource.engine;

import java.io.File;

import adn.application.context.ContextProvider;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.template.ResourceTemplate;

/**
 * @author Ngoc Huy
 *
 */
public interface LocalStorage {

	public static final Finder finder = ContextProvider.getApplicationContext().getBean(Finder.class);

	void registerTemplate(ResourceTemplate template) throws IllegalArgumentException;

	ResultSetImplementor query(Query query);

	ResultSetImplementor execute(Query query);

	ResourceTemplate getResourceTemplate(String templateName);

	File instantiate(Query query, ResourceTemplate template);

}
