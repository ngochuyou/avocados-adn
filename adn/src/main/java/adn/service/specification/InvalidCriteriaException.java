/**
 * 
 */
package adn.service.specification;

/**
 * @author Ngoc Huy
 *
 */
public class InvalidCriteriaException extends Throwable {

	private static final long serialVersionUID = 1L;

	private InvalidCriteriaException() {}
	
	public static final InvalidCriteriaException INSTANCE = new InvalidCriteriaException();
	
}
