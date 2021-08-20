/**
 * 
 */
package adn.model.factory.authentication.dynamic;

import java.util.List;

import adn.model.DomainEntity;
import adn.model.entities.metadata.DomainEntityMetadata;

/**
 * @author Ngoc Huy
 *
 */
public class BatchedPojoSourceImpl<T extends DomainEntity> extends AbstractPojoSource<T, List<T>> {

	private final List<T> source;

	public BatchedPojoSourceImpl(String[] columns, DomainEntityMetadata metadata, Class<T> type, List<T> source) {
		super(columns, metadata, type);
		this.source = source;
	}

	@Override
	public List<T> getSource() {
		return source;
	}

}
