/**
 * 
 */
package adn.service.resource.event;

/**
 * @author Ngoc Huy
 *
 */
public interface EventExecutor<E, T extends Event<E>> {

	void execute(T event);

}
