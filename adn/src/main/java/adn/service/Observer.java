/**
 * 
 */
package adn.service;

/**
 * @author Ngoc Huy
 *
 */
public interface Observer {

	void doNotify() throws Exception;

	String getId();
	
}
