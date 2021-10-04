/**
 * 
 */
package adn.application.context.internal;

/**
 * @author Ngoc Huy
 *
 */
public interface ContextBuilder {

	void buildAfterStartUp() throws Exception;

	default void afterBuild() {}

}
