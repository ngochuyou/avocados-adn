/**
 * 
 */
package adn.model.factory.authentication.dynamicmap;

import adn.model.factory.authentication.Arguments;
import adn.model.factory.authentication.SourceArguments;
import adn.model.factory.authentication.SourceMetadata;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractSourceArgument<T> implements SourceArguments<T> {

	private final SourceMetadata metadata;

	public AbstractSourceArgument(SourceMetadata metadata) {
		super();
		this.metadata = metadata;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <X extends Arguments<T>, E extends X> E unwrap(Class<E> type) {
		return (E) this;
	}

	@Override
	public SourceMetadata getMetadata() {
		return metadata;
	}

}
