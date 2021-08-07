/**
 * 
 */
package adn.service.specification;

/**
 * @author Ngoc Huy
 *
 */
public class EmptyPredicateException extends Throwable {

	private static final long serialVersionUID = 1L;

	private EmptyPredicateException() {}
	
	public static final EmptyPredicateException INSTANCE = new EmptyPredicateException();
	
}
