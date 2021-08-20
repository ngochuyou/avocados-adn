/**
 * 
 */
package adn.model.factory.authentication.dynamic;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import adn.model.entities.metadata.DomainEntityMetadata;
import adn.model.factory.authentication.Arguments;
import adn.model.factory.authentication.SourceArguments;
import io.jsonwebtoken.lang.Collections;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractSourceArgument<T> implements SourceArguments<T> {

	private final String[] columns;
	private final String[] associationColumns;
	private final Set<Integer> associationIndicies;

	public AbstractSourceArgument(String[] columns, DomainEntityMetadata metadata) {
		super();
		this.columns = columns;

		int span = columns.length;

		Set<Integer> associationIndicies = new HashSet<>(columns.length);
		String[] associationColumns = IntStream.range(0, span).mapToObj(index -> {
			associationIndicies.add(index);
			return columns[index];
		}).toArray(String[]::new);

		if (associationColumns.length != 0) {
			this.associationColumns = associationColumns;
			this.associationIndicies = associationIndicies;
			return;
		}

		this.associationColumns = null;
		this.associationIndicies = null;
	}

	@Override
	public String[] getColumns() {
		return columns;
	}

	@Override
	public boolean hasAssociation() {
		return !Collections.isEmpty(associationIndicies);
	}

	@Override
	public String[] getAssociationColumns(int index) {
		return associationColumns;
	}

	@Override
	public Set<Integer> getAssociationIndicies() {
		return associationIndicies;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <X extends Arguments<T>, E extends X> E unwrap(Class<E> type) {
		return (E) this;
	}

}
