/**
 * 
 */
package adn.application.context;

/**
 * @author Ngoc Huy
 *
 */
public interface EffectivelyFinal {

	Access getAccess() throws IllegalAccessException;
	
	public interface Access {
		
		void close();
		
		default void execute() throws Exception {
			close();
		}

	}

}
