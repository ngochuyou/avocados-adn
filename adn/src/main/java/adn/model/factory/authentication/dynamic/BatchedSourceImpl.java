/**
 * 
 */
package adn.model.factory.authentication.dynamic;

import java.util.List;

import adn.model.entities.metadata.DomainEntityMetadata;

/**
 * @author Ngoc Huy
 *
 */
public class BatchedSourceImpl extends AbstractSourceArgument<List<Object[]>> {

	private final List<Object[]> source;

	public BatchedSourceImpl(String[] columns, DomainEntityMetadata metadata, List<Object[]> source) {
		super(columns, metadata);
		this.source = source;
	}

	@Override
	public List<Object[]> getSource() {
		return source;
	}

}
