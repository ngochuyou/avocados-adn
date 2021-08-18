/**
 * 
 */
package adn.application.context.internal;

/**
 * @author Ngoc Huy
 *
 */
public interface ContextBuilder {

	String CONFIG_PATH = "C:\\Users\\Ngoc Huy\\Documents\\workspace\\avocados-adn\\config\\";

	void buildAfterStartUp() throws Exception;

	default void afterBuild() {}

}
