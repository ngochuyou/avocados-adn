/**
 * 
 */
package adn.application;

/**
 * @author Ngoc Huy
 *
 */
public interface Loggable {

	default String getLoggableName() {
		return this.getClass().getSimpleName();
	}
	
}
