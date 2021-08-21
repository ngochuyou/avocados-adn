/**
 * 
 */
package adn.model.factory.authentication.dynamicmap;

import adn.model.factory.authentication.SingleSource;
import adn.model.factory.authentication.SourceMetadata;

/**
 * @author Ngoc Huy
 *
 */
public class SingleSourceImpl extends AbstractSourceArgument<Object[]> implements SingleSource {

	private final Object[] source;

	public SingleSourceImpl(SourceMetadata metadata, Object[] source) {
		super(metadata);
		this.source = source;
	}

	@Override
	public Object[] getSource() {
		return source;
	}

}
