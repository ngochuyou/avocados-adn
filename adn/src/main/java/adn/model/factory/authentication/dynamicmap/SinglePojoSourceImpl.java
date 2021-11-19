/**
 * 
 */
package adn.model.factory.authentication.dynamicmap;

import adn.model.DomainEntity;
import adn.model.factory.authentication.SinglePojoSource;
import adn.model.factory.authentication.SourceMetadata;

/**
 * @author Ngoc Huy
 *
 */
public class SinglePojoSourceImpl<T extends DomainEntity> extends AbstractSourceArgument<T, T>
		implements SinglePojoSource<T> {

	private final T source;

	public SinglePojoSourceImpl(SourceMetadata<T> metadata, T source) {
		super(metadata);
		this.source = source;
	}

	@Override
	public T getSource() {
		return source;
	}

}
