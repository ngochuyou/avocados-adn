/**
 * 
 */
package adn.service.resource.engine.query;

import java.sql.SQLException;
import java.util.Iterator;

/**
 * @author Ngoc Huy
 *
 */
public interface Query {

	QueryCompiler.QueryType getType();

	String getTemplateName();

	Iterator<String> getColumnNames();

	Object[] getParameters();
	
	Query clear();
	
	default Query addParameter(int index, Object param) throws SQLException {
		return this;
	}

	default Query lockQuery() {
		return this;
	}

	default Query unLockQuery() {
		return this;
	}

}
