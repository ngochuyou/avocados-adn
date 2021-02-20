/**
 * 
 */
package adn.service.resource.metamodel;

/**
 * @author Ngoc Huy
 *
 */
public interface NamingStrategyDelegate {

	String determineResourceName(Object resource) throws Exception;

}
