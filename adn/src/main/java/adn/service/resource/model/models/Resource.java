/**
 * 
 */
package adn.service.resource.model.models;

/**
 * @author Ngoc Huy
 *
 */
public interface Resource {

	String getName();

	String getExtension();

	String RESOURCE_IDENTIFIER_ATTRIBUTE_NAME = "name";

	String RESOURCE_CONTENT_ATTRIBUTE_NAME = "content";

}
