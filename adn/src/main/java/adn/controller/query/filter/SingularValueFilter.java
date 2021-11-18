/**
 * 
 */
package adn.controller.query.filter;

/**
 * @author Ngoc Huy
 *
 */
public interface SingularValueFilter<T> {

	/**
	 * @return equals value
	 */
	T getEquals();

	/**
	 * @return not equals value
	 */
	T getNe();

	/**
	 * @return like value
	 */
	String getLike();

	/**
	 * @return is value
	 */
	T getIs();

	/**
	 * @return is not value
	 */
	T getIsNot();
	
	/**
	 * @return from value
	 */
	T getFrom();
	
	/**
	 * @return to value
	 */
	T getTo();

}
