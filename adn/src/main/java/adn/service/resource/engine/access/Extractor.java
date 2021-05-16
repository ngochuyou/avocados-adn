/**
 * 
 */
package adn.service.resource.engine.access;

import org.hibernate.HibernateException;

import adn.helpers.FunctionHelper.HandledFunction;

/**
 * @author Ngoc Huy
 *
 */
public interface Extractor<T, R> extends HandledFunction<T, R, HibernateException> {

}
