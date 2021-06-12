/**
 * 
 */
package adn.service.resource.engine.query;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adn.helpers.StringHelper;
import adn.service.resource.factory.ManagerFactory;

/**
 * @author Ngoc Huy
 *
 */
public final class QueryCompiler {

	public static enum QueryType {

		SAVE, FIND, UPDATE, DELETE, REGISTER_TEMPLATE, UNKNOWN;

		public static QueryType determineType(String sqlString) throws SQLException {
			String firstWord = StringHelper.getFirstWord(sqlString).toLowerCase();

			switch (firstWord) {
				case "insert": {
					return QueryType.SAVE;
				}
				case "select": {
					return QueryType.FIND;
				}
				case "update": {
					return QueryType.UPDATE;
				}
				case "delete": {
					return QueryType.DELETE;
				}
				case "register_template": {
					return QueryType.REGISTER_TEMPLATE;
				}
				default: {
					return QueryType.UNKNOWN;
				}
			}
		}

	}

	private QueryCompiler() {}

	private static final Pattern SELECT_PATTERN;
	public static final Pattern SELECT_COLUMNS_PATTERN;
	public static final Pattern SELECT_COLUMN_PATTERN;

	private static final Pattern INSERT_PATTERN = Pattern
			.compile("((?<=(insert\\sinto\\s))[\\w\\d_]+(?=\\s+))|((?<=\\()([\\w\\d_,\\s+]+)+(?=\\)))|values\\s+.+");
	private static final Pattern UPDATE_PATTERN;

	private static final String FROM_TABLENAME_GROUP_NAME = "tablename";
	private static final String SET_STATEMENTGROUP_GROUP_NAME = "statements";
	private static final Pattern SET_STATEMENT_PATTERN;
	private static final Pattern WHERE_CONDITION_PATTERN;
	private static final String WHERE_CONDITIONS_GROUP_NAME = "conditions";
	private static final String STATEMENT_COLUMNNAME_GROUP_NAME = "name";
	private static final String STATEMENT_VALUE_GROUP_NAME = "value";
	public static final String COLUMNGROUP_GROUP_NAME = "columns";
	public static final String COLUMNGROUP_ACTUAL_NAME_GROUP_NAME = "actualcolumnname";
	public static final String COLUMNGROUP_ALIAS_NAME_GROUP_NAME = "columnalias";

	static {
		String mark = "\\?,\\.''\"\\s\\t\\n><\\=\\(\\)";
		String letter = "\\w\\d_";
		String ops = "(\\=|like|LIKE|is|IS|>|<)";
		// @formatter:off
		SELECT_PATTERN = Pattern.compile(
			String.format(""
				+ "(select|SELECT)\\s+(?<%s>[%s]+)\\s*+"
				+ "(from|FROM)\\s+(?<%s>[%s]+)(\\s*+|[%s]+\\s)"
				+ "((where|WHERE)\\s+(?<%s>[%s]+)\\s*+)?",
				COLUMNGROUP_GROUP_NAME, mark + letter,
				FROM_TABLENAME_GROUP_NAME, letter, letter + mark,
				WHERE_CONDITIONS_GROUP_NAME, letter + mark + ops));
		
		WHERE_CONDITION_PATTERN = Pattern.compile(
			String.format(""
				+ "([%s]+"
				+ "\\.)?"
				+ "(?<%s>[%s]+)"
				+ "\\s*+%s\\s*+"
				+ "(?<%s>[%s]+)",
				letter,
				STATEMENT_COLUMNNAME_GROUP_NAME, letter,
				ops,
				STATEMENT_VALUE_GROUP_NAME, letter + mark));
		
		UPDATE_PATTERN = Pattern.compile(
			String.format(""
				+ "(update|UPDATE)\\s+(?<%s>[%s]+)(\\s+|[%s]+\\s)"
				+ "(set|SET)\\s+(?<%s>[%s]+)\\s+"
				+ "(where|WHERE)\\s+(?<%s>[%s]+)",
				FROM_TABLENAME_GROUP_NAME, letter, letter,
				SET_STATEMENTGROUP_GROUP_NAME, mark + letter,
				WHERE_CONDITIONS_GROUP_NAME, letter + mark + ops));
		
		SELECT_COLUMNS_PATTERN = Pattern.compile(
			String.format(
				"select\\s+(?<%s>[%s]+)\\sfrom[%s]+",
				COLUMNGROUP_GROUP_NAME, letter + mark, letter + mark + ops)
		);
		
		SELECT_COLUMN_PATTERN = Pattern.compile(
			String.format(""
				+ "^[%s]+"
				+ "\\."
				+ "(?<%s>[%s]+)"
				+ "(\\s*+as\\s*+"
				+ "(?<%s>[%s]+))?",
				letter,
				COLUMNGROUP_ACTUAL_NAME_GROUP_NAME, letter,
				COLUMNGROUP_ALIAS_NAME_GROUP_NAME, letter)
		);
		
		SET_STATEMENT_PATTERN = Pattern.compile(
			String.format(""
				+ "(?<%s>[%s]+)"
				+ "\\s*+\\=\\s*+"
				+ "(?<%s>[%s]+)",
				STATEMENT_COLUMNNAME_GROUP_NAME, letter,
				STATEMENT_VALUE_GROUP_NAME, letter + mark)
		);
		// @formatter:on
	}

	public static Query compile(Query other) throws SQLException {
		return new QueryImpl(other);
	}

	public static Query compile(String sql, Statement statement) throws SQLException {
		QueryImpl query = new QueryImpl().setStatement(statement).setActualSQLString(sql)
				.setQueryType(QueryType.determineType(sql));

		switch (query.getType()) {
			case FIND: {
				return compileFind(query, sql.trim()).lockQuery();
			}
			case SAVE: {
				return compileSave(query, sql.trim()).lockQuery();
			}
			case UPDATE: {
				return compileUpdate(query, sql.trim()).lockQuery();
			}
			default:
				throw new SQLException(String.format("Unable to compile query [%s], unknown query type", sql));
		}
	}

	public static final String SET_MARKER = "set$";
	public static final String WHERE_MARKER = "where$";

	private static Query compileUpdate(QueryImpl query, String sql) throws SQLException {
		Matcher matcher = UPDATE_PATTERN.matcher(sql);

		if (!matcher.matches()) {
			throw new SQLException(String.format("Invalid query [%s]", sql));
		}

		try {
			String templateName = matcher.group(FROM_TABLENAME_GROUP_NAME) + ManagerFactory.DTYPE_SEPERATOR;

			if (matcher.group(SET_STATEMENTGROUP_GROUP_NAME) == null) {
				throw new SQLException(
						String.format("Query [%s] doesn't include any SET portion, expect at least 1", sql));
			}

			String[] parts = StringHelper.removeSpaces(matcher.group(SET_STATEMENTGROUP_GROUP_NAME)).split(",");
			Matcher innerMatcher;

			for (String statement : parts) {
				if ((innerMatcher = SET_STATEMENT_PATTERN.matcher(statement)).matches()) {
					if (innerMatcher.group(STATEMENT_COLUMNNAME_GROUP_NAME).equals(ManagerFactory.DTYPE_COLUMNNAME)) {
						continue;
					}

					query.addColumnName(
							String.format("%s%s", SET_MARKER, innerMatcher.group(STATEMENT_COLUMNNAME_GROUP_NAME)));
					continue;
				}

				throw new SQLException(String.format("Invalid set statement pattern [%s]", statement));
			}

			if (matcher.group(WHERE_CONDITIONS_GROUP_NAME) == null) {
				throw new SQLException(
						String.format("Query [%s] doesn't include any WHERE portion, expect at least 1", sql));
			}

			parts = matcher.group(WHERE_CONDITIONS_GROUP_NAME).split("\\sand\\s");

			for (String condition : parts) {
				if ((innerMatcher = WHERE_CONDITION_PATTERN.matcher(condition)).matches()) {
					if (innerMatcher.group(STATEMENT_COLUMNNAME_GROUP_NAME).equals(ManagerFactory.DTYPE_COLUMNNAME)) {
						templateName += innerMatcher.group(STATEMENT_VALUE_GROUP_NAME).replaceAll("'", "");
						continue;
					}

					query.addColumnName(
							String.format("%s%s", WHERE_MARKER, innerMatcher.group(STATEMENT_COLUMNNAME_GROUP_NAME)));
					continue;
				}

				throw new SQLException(String.format("Invalid where statement pattern [%s]", condition));
			}

			return query.setTemplateName(templateName);
		} catch (RuntimeException any) {
			throw new SQLException(any);
		}
	}

	private static Query compileFind(QueryImpl query, String sql) throws SQLException {
		Matcher matcher = SELECT_PATTERN.matcher(sql);

		if (!matcher.matches()) {
			throw new SQLException(String.format("Invalid query [%s]", sql));
		}

		try {
			String templateName = matcher.group(FROM_TABLENAME_GROUP_NAME) + ManagerFactory.DTYPE_SEPERATOR;

			if (matcher.group(WHERE_CONDITIONS_GROUP_NAME) == null) {
				return query.setTemplateName(templateName);
			}

			String conditions[] = matcher.group(WHERE_CONDITIONS_GROUP_NAME).split("\\sand\\s");
			Matcher conditionMatcher;

			for (String condition : conditions) {
				if ((conditionMatcher = WHERE_CONDITION_PATTERN.matcher(condition)).matches()) {
					if (conditionMatcher.group(STATEMENT_COLUMNNAME_GROUP_NAME)
							.equals(ManagerFactory.DTYPE_COLUMNNAME)) {
						templateName += conditionMatcher.group(STATEMENT_VALUE_GROUP_NAME).replaceAll("'", "");
						continue;
					}

					query.addColumnName(conditionMatcher.group(STATEMENT_COLUMNNAME_GROUP_NAME));
					continue;
				}

				throw new SQLException(String.format("Invalid where pattern [%s]", condition));
			}

			return query.setTemplateName(templateName);
		} catch (RuntimeException any) {
			throw new SQLException(any);
		}
	}

	private static Query compileSave(QueryImpl query, String sql) throws SQLException {
		Matcher matcher = INSERT_PATTERN.matcher(sql);
		String templateName;

		try {
			matcher.find();
			templateName = matcher.group() + ManagerFactory.DTYPE_SEPERATOR;
			matcher.find();

			String[] columnNames = StringHelper.removeSpaces(matcher.group()).split(",");

			for (String columnName : columnNames) {
				if (columnName.equals(ManagerFactory.DTYPE_COLUMNNAME)) {
					matcher.find();
					templateName += matcher.group().split("\\'")[1];
					continue;
				}

				query.addColumnName(columnName);
			}

			return query.setTemplateName(templateName);
		} catch (RuntimeException any) {
			throw new SQLException(any);
		}
	}

}
