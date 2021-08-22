/**
 * 
 */
package adn.model.factory.authentication.dynamicmap;

import java.util.List;

import adn.model.DomainEntity;
import adn.model.factory.authentication.BatchedSource;
import adn.model.factory.authentication.SourceMetadata;

/**
 * @author Ngoc Huy
 *
 */
public class BatchedSourceImpl<E extends DomainEntity> extends AbstractSourceArgument<E, List<Object[]>> implements BatchedSource<E> {

	private final List<Object[]> source;

	public BatchedSourceImpl(SourceMetadata<E> metadata, List<Object[]> source) {
		super(metadata);
		this.source = source;
	}

	@Override
	public List<Object[]> getSource() {
		return source;
	}

}
