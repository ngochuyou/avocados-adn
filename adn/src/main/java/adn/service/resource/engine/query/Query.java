/**
 * 
 */
package adn.service.resource.engine.query;

import adn.helpers.StringHelper;

/**
 * @author Ngoc Huy
 *
 */
public interface Query {

	public enum QueryType {

		INSERT, FIND, DELETE, REGISTER_TEMPLATE;

		public QueryType determineType(String sqlString) {
			String firstWord = StringHelper.getFirstWord(sqlString).toLowerCase();

			switch (firstWord) {
				case "insert": {
					return QueryType.INSERT;
				}
				case "select": {
					return QueryType.FIND;
				}
				case "delete": {
					return QueryType.DELETE;
				}
				case "register_template": {
					return QueryType.REGISTER_TEMPLATE;
				}
				default: {
					throw new IllegalArgumentException(
							String.format("Unable to determine query type for [%s]", sqlString));
				}
			}
		}

	}

	QueryType getType();

	String[] getColumnNames();

	Object[] getParameters();

}
