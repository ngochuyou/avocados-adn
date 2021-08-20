/**
 * 
 */
package adn.model.factory.authentication.dynamic;

import adn.model.DomainEntity;
import adn.model.entities.metadata.DomainEntityMetadata;

/**
 * @author Ngoc Huy
 *
 */
public class SinglePojoSourceImpl<T extends DomainEntity> extends AbstractPojoSource<T, T> {

	private final T source;

	public SinglePojoSourceImpl(String[] columns, Class<T> type, DomainEntityMetadata metadata, T source) {
		super(columns, metadata, type);
		this.source = source;
	}

	@Override
	public T getSource() {
		return source;
	}

}
