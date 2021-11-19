/**
 * 
 */
package adn.model.factory.authentication.dynamicmap;

import adn.model.DomainEntity;
import adn.model.factory.authentication.SingleSource;
import adn.model.factory.authentication.SourceMetadata;

/**
 * @author Ngoc Huy
 *
 */
public class SingleSourceImpl<T extends DomainEntity> extends AbstractSourceArgument<T, Object[]> implements SingleSource<T> {

	private final Object[] source;

	public SingleSourceImpl(SourceMetadata<T> metadata, Object[] source) {
		super(metadata);
		this.source = source;
	}

	@Override
	public Object[] getSource() {
		return source;
	}

}
