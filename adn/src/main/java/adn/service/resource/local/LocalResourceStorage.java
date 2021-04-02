/**
 * 
 */
package adn.service.resource.local;

/**
 * @author Ngoc Huy
 *
 */
public interface LocalResourceStorage extends Service {

	boolean isExists(String filename);

}
