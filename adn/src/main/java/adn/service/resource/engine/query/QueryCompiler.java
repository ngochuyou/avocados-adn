/**
 * 
 */
package adn.service.resource.engine.query;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adn.helpers.StringHelper;

/**
 * @author Ngoc Huy
 *
 */
public final class QueryCompiler {

	public static enum QueryType {

		INSERT, FIND, DELETE, REGISTER_TEMPLATE;

		public static QueryType determineType(String sqlString) throws SQLException {
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
					throw new SQLException(String.format("Unable to determine query type for [%s]", sqlString));
				}
			}
		}

	}

	private QueryCompiler() {}

	private static final String INSERT_REGEX = "((?<=(insert\\sinto\\s))[\\w\\d_]+(?=\\s+))|((?<=\\()([\\w\\d_,\\s+]+)+(?=\\)))|values\\s+.+";
	private static final Pattern INSERT_PATTERN = Pattern.compile(INSERT_REGEX);

	public static Query compile(String sql) throws SQLException {
		QueryImpl query = new QueryImpl().setQueryType(QueryType.determineType(sql));
		Matcher matcher = INSERT_PATTERN.matcher(sql);
		String templateName;

		try {
			matcher.find();
			templateName = matcher.group();
			matcher.find();

			String[] columnNames = StringHelper.removeSpaces(matcher.group()).split(",");

			for (String columnName : columnNames) {
				if (columnName.equals("DTYPE")) {
					matcher.find();
					templateName += ("#" + matcher.group().split("\\'")[1]);
					continue;
				}

				query.addColumnName(columnName);
			}

			query.setTemplateName(templateName);
		} catch (Exception e) {
			throw new SQLException(e);
		}

		return query.lockQuery();
	}

}
