/**
 * 
 */
package adn.model.factory.authentication;

import java.util.List;
import java.util.function.BiFunction;

/**
 * @author Ngoc Huy
 *
 */
public interface ModelProducer<S, P> {

	static final BiFunction<Arguments<?>, Credential, Object> MASKER = (any, credential) -> null;
	static final BiFunction<Arguments<?>, Credential, Object> PUBLISHER = (any, credential) -> any;

	P produce(S source, Credential credential);

	List<P> produce(List<S> source, Credential credential);

}
