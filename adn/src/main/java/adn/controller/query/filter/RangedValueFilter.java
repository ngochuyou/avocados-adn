/**
 * 
 */
package adn.controller.query.filter;

import adn.helpers.Utils;

/**
 * @author Ngoc Huy
 *
 */
public interface RangedValueFilter<T> {

	/**
	 * @return greaterThan value
	 */
	T getGt();

	/**
	 * @return greaterThanOrEquals value
	 */
	T getGtoE();

	/**
	 * @return lesserThan value
	 */
	T getLt();

	/**
	 * @return lesserThanOrEquals value
	 */
	T getLtoE();

	/**
	 * @return between value
	 */
	Utils.Entry<T, T> getBetween();

}
