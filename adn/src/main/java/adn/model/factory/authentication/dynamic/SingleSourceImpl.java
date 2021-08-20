/**
 * 
 */
package adn.model.factory.authentication.dynamic;

import adn.model.entities.metadata.DomainEntityMetadata;
import adn.model.factory.authentication.SingleSource;

/**
 * @author Ngoc Huy
 *
 */
public class SingleSourceImpl extends AbstractSourceArgument<Object[]> implements SingleSource {

	private final Object[] source;

	public SingleSourceImpl(String[] columns, DomainEntityMetadata metadata, Object[] source) {
		super(columns, metadata);
		this.source = source;
	}

	@Override
	public Object[] getSource() {
		return source;
	}

}
