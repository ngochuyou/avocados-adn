/**
 * 
 */
package adn.model.factory.authentication.dynamicmap;

import adn.model.DomainEntity;
import adn.model.factory.authentication.Arguments;
import adn.model.factory.authentication.SourceArguments;
import adn.model.factory.authentication.SourceMetadata;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractSourceArgument<E extends DomainEntity, T> implements SourceArguments<E, T> {

	private final SourceMetadata<E> metadata;

	public AbstractSourceArgument(SourceMetadata<E> metadata) {
		super();
		this.metadata = metadata;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <X extends Arguments<T>, Y extends X> Y unwrap(Class<Y> type) {
		return (Y) this;
	}

	@Override
	public SourceMetadata<E> getMetadata() {
		return metadata;
	}

}
