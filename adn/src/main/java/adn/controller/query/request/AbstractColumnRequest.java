/**
 * 
 */
package adn.controller.query.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import adn.controller.query.ColumnsRequest;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractColumnRequest implements ColumnsRequest {

	private List<String> columns = new ArrayList<>();

	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	@Override
	public boolean isEmpty() {
		return join().isEmpty();
	}

	@Override
	public Collection<String> join() {
		List<String> columnsCopy = new ArrayList<>(columns);

		columnsCopy
				.addAll(getAssociationColumns().stream().flatMap(list -> list.stream()).collect(Collectors.toList()));

		return columnsCopy;
	}

	protected abstract List<List<String>> getAssociationColumns();

}
