/**
 * 
 */
package adn.model.factory;

import adn.model.AbstractModel;

/**
 * @author Ngoc Huy
 *
 */
public interface ModelProducer<T extends AbstractModel, P> {

	P produce(T entity);

	P produceImmutable(T entity);

}
