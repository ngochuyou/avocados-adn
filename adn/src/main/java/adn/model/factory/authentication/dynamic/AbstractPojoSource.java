/**
 * 
 */
package adn.model.factory.authentication.dynamic;

import adn.model.DomainEntity;
import adn.model.entities.metadata.DomainEntityMetadata;
import adn.model.factory.authentication.PojoSource;

public abstract class AbstractPojoSource<Y extends DomainEntity, P> extends AbstractSourceArgument<P>
		implements PojoSource<Y, P> {

	private final Class<Y> type;

	public AbstractPojoSource(String[] columns, DomainEntityMetadata metadata, Class<Y> type) {
		super(columns, metadata);
		this.type = type;
	}

	@Override
	public Class<Y> getType() {
		return type;
	}

}