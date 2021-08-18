/**
 * 
 */
package adn.service.resource.engine.query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ngoc Huy
 *
 */
public class UpdateQueryImpl extends QueryImpl implements UpdateQuery {

	private Map<String, Integer> whereColumnsIndexMap = new HashMap<>();
	private ArrayList<String> whereColumnNames = new ArrayList<>();
	private ArrayList<Object> conditionValues = new ArrayList<>();

	private UpdateQuery next;

	UpdateQueryImpl(QueryImpl query) {
		super(query);
	}

	@Override
	public void batch(Query query) {
		if (query instanceof UpdateQuery) {
			if (query.equals(this)) {
				return;
			}

			if (this.next == null) {
				this.next = (UpdateQuery) query;
				return;
			}

			this.next.batch(query);
			return;
		}

		throw new IllegalArgumentException(
				String.format("Unable to batch non-UpdateQuery. Given query is [%s]", query.toString()));
	}

	UpdateQueryImpl addWhereStatementColumnName(String name) throws SQLException {
		checkLock();
		whereColumnNames.add(name);
		conditionValues.add(null);
		whereColumnsIndexMap.put(name, whereColumnNames.size() - 1);
		return this;
	}

	@Override
	public UpdateQuery setWhereStatementParameterValue(int index, Object value) {
		int actualIndex = index - 1;

		conditionValues.set(actualIndex, value);
		return this;
	}

	@Override
	public UpdateQuery setWhereStatementParameterValue(String columnName, Object value) {
		conditionValues.set(whereColumnsIndexMap.get(columnName), value);
		return this;
	}

	@Override
	public Query setParameterValue(int index, Object value) throws SQLException {
		if (index <= super.columnNames.size()) {
			return super.setParameterValue(index, value);
		}

		return this.setWhereStatementParameterValue(index - columnNames.size(), value);
	}

	@Override
	public Query setParameterValue(String name, Object param) throws SQLException {
		return super.setParameterValue(name, param);
	}

	@Override
	public Object getWhereConditionValue(String whereColumnName) {
		return conditionValues.get(whereColumnsIndexMap.get(whereColumnName));
	}

	@Override
	public UpdateQuery next() {
		return next;
	}

	@Override
	public String[] getWhereStatementColumnNames() {
		return whereColumnNames.toArray(String[]::new);
	}

}
