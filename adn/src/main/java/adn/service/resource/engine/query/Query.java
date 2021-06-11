/**
 * 
 */
package adn.service.resource.engine.query;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
 * @author Ngoc Huy
 *
 */
public interface Query {

	void batch(Query query);

	Query next();

	QueryCompiler.QueryType getType();

	String getTemplateName();

	Object getParameterValue(String paramName);

	Collection<String> getParameterNames();

	Query clear();

	String getActualSQLString();

	Statement getStatement();

	default Query setParameterValue(int i, Object value) throws SQLException {
		return this;
	}

	default Query setParameterValue(String name, Object param) throws SQLException {
		return this;
	}

	default Query lockQuery() {
		return this;
	}

	default Query unLockQuery() {
		return this;
	}

}
