/**
 * 
 */
package adn.model.factory.authentication;

import java.util.List;

import adn.helpers.FunctionHelper.HandledBiFunction;

/**
 * @author Ngoc Huy
 *
 */
public interface ModelProducer<S, P> {

	static final HandledBiFunction<Arguments<?>, Credential, Object, Exception> MASKER = (any, credential) -> null;
	static final HandledBiFunction<Arguments<?>, Credential, Object, Exception> PUBLISHER = (any, credential) -> any.getSource();

	P produce(S source, Credential credential);

	List<P> produce(List<S> source, Credential credential);

}
