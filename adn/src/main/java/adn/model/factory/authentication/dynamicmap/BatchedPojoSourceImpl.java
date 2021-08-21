/**
 * 
 */
package adn.model.factory.authentication.dynamicmap;

import java.util.List;

import adn.model.DomainEntity;
import adn.model.factory.authentication.BatchedPojoSource;
import adn.model.factory.authentication.SourceMetadata;

/**
 * @author Ngoc Huy
 *
 */
public class BatchedPojoSourceImpl<T extends DomainEntity> extends AbstractSourceArgument<List<T>>
		implements BatchedPojoSource<T> {

	private final List<T> source;

	public BatchedPojoSourceImpl(SourceMetadata metadata, List<T> source) {
		super(metadata);
		this.source = source;
	}

	@Override
	public List<T> getSource() {
		return source;
	}

}
