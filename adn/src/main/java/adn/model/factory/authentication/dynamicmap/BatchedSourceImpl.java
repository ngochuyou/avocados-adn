/**
 * 
 */
package adn.model.factory.authentication.dynamicmap;

import java.util.List;

import adn.model.factory.authentication.BatchedSource;
import adn.model.factory.authentication.SourceMetadata;

/**
 * @author Ngoc Huy
 *
 */
public class BatchedSourceImpl extends AbstractSourceArgument<List<Object[]>> implements BatchedSource {

	private final List<Object[]> source;

	public BatchedSourceImpl(SourceMetadata metadata, List<Object[]> source) {
		super(metadata);
		this.source = source;
	}

	@Override
	public List<Object[]> getSource() {
		return source;
	}

}
