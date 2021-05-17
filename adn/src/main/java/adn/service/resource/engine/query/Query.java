/**
 * 
 */
package adn.service.resource.engine.query;

import java.sql.SQLException;

/**
 * @author Ngoc Huy
 *
 */
public interface Query {

	QueryCompiler.QueryType getType();

	String getTemplateName();

	Object getParameterValue(String paramName);

	Query clear();

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
