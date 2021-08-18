/**
 * 
 */
package adn.model.factory;

import java.util.List;

/**
 * @author Ngoc Huy
 *
 */
public interface ModelProducer<S, P> {

	P produce(S source);

	List<P> produce(List<S> sources);

}
