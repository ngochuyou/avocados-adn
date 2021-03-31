/**
 * 
 */
package adn.service.resource.event;

/**
 * @author Ngoc Huy
 *
 */
public interface EventExecutor<T extends Event> {

	void execute(T event);

}
