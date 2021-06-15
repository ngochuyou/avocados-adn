/**
 * 
 */
package adn.service.resource.engine.query;

import java.sql.SQLException;

/**
 * @author Ngoc Huy
 *
 */
public interface UpdateQuery extends Query {

	Object getWhereConditionValue(String whereColumnName);

	String[] getWhereStatementColumnNames();

	default UpdateQuery setWhereStatementParameterValue(int i, Object value) throws SQLException {
		return this;
	}

	default UpdateQuery setWhereStatementParameterValue(String name, Object param) throws SQLException {
		return this;
	}

	@Override
	UpdateQuery next();

}
