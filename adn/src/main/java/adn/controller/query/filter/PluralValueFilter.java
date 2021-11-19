/**
 * 
 */
package adn.controller.query.filter;

/**
 * @author Ngoc Huy
 *
 */
public interface PluralValueFilter<T> {

	T[] getIn();

	T[] getNotIn();

}
