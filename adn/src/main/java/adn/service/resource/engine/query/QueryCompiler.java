/**
 * 
 */
package adn.service.resource.engine.query;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adn.helpers.StringHelper;
import adn.service.resource.factory.ResourceManagerFactory;

/**
 * @author Ngoc Huy
 *
 */
public final class QueryCompiler {

	public static enum QueryType {

		SAVE, FIND, UPDATE, DELETE, REGISTER_TEMPLATE, UNKNOWN;

		private static final Map<String, QueryType> QUERY_TYPE_MAP;
		private static final Map<String, String> QUERY_TYPE_MAP_KEY_MAP;

		static {
			Map<String, QueryType> typeMap = new HashMap<>();
			Map<String, String> keyMap = new HashMap<>();

			String insertKey = "insert", selectKey = "select", updateKey = "update", deleteKey = "delete",
					registerTemplateKey = "register_template";

			typeMap.put(insertKey, SAVE);
			typeMap.put(selectKey, FIND);
			typeMap.put(updateKey, UPDATE);
			typeMap.put(deleteKey, DELETE);
			typeMap.put(registerTemplateKey, REGISTER_TEMPLATE);
			typeMap.put(null, UNKNOWN);

			keyMap.put(insertKey, insertKey);
			keyMap.put(selectKey, selectKey);
			keyMap.put(updateKey, updateKey);
			keyMap.put(deleteKey, deleteKey);
			keyMap.put(registerTemplateKey, registerTemplateKey);

			QUERY_TYPE_MAP_KEY_MAP = Collections.unmodifiableMap(keyMap);
			QUERY_TYPE_MAP = Collections.unmodifiableMap(typeMap);
		}

		public static QueryType determineType(String sqlString) throws SQLException {
			return QUERY_TYPE_MAP.get(QUERY_TYPE_MAP_KEY_MAP.get(StringHelper.getFirstWord(sqlString).toLowerCase()));
		}

	}

	private QueryCompiler() {}

	private static final Pattern SELECT_PATTERN;
	public static final Pattern SELECT_COLUMNS_PATTERN;
	public static final Pattern SELECT_COLUMN_PATTERN;

	private static final Pattern INSERT_PATTERN;

	private static final Pattern UPDATE_PATTERN;
	private static final Pattern SET_STATEMENT_PATTERN;
	private static final Pattern WHERE_CONDITION_PATTERN;

	private static final Pattern DELETE_PATTERN;

	private static final String TABLENAME_GROUP_NAME = "tablename";
	private static final String SET_STATEMENTS_GROUP_NAME = "statements";
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
		INSERT_PATTERN = Pattern.compile(String.format(""
				+ "(insert|INSERT)\\s+(into|INTO)\\s+(?<%s>[%s]+)\\s+"
				+ "\\((?<%s>[%s]+)\\)\\s+"
				+ "(values|VALUES)\\s+\\((?<%s>[%s]+)\\)\\s?",
				TABLENAME_GROUP_NAME, letter,
				COLUMNGROUP_GROUP_NAME, letter + mark,
				SET_STATEMENTS_GROUP_NAME, letter + mark));
		
		SELECT_PATTERN = Pattern.compile(
			String.format(""
				+ "(select|SELECT)\\s+(?<%s>[%s]+)\\s*+"
				+ "(from|FROM)\\s+(?<%s>[%s]+)(\\s*+|[%s]+\\s)"
				+ "((where|WHERE)\\s+(?<%s>[%s]+)\\s?)?",
				COLUMNGROUP_GROUP_NAME, mark + letter,
				TABLENAME_GROUP_NAME, letter, letter + mark,
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
				TABLENAME_GROUP_NAME, letter, letter,
				SET_STATEMENTS_GROUP_NAME, mark + letter,
				WHERE_CONDITIONS_GROUP_NAME, letter + mark + ops));
		
		SELECT_COLUMNS_PATTERN = Pattern.compile(
			String.format(
				"(select|SELECT)\\s+(?<%s>[%s]+)\\s(from|FROM)[%s]+",
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
		
		DELETE_PATTERN = Pattern.compile(
			String.format(""
					+ "(delete|DELETE)\\s+(from|FROM)\\s+(?<%s>[%s]+)\\s+"
					+ "((where|WHERE)\\s+(?<%s>[%s]+)\\s?)?",
					TABLENAME_GROUP_NAME, letter,
					WHERE_CONDITIONS_GROUP_NAME, letter + mark + ops)
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
				return compileUpdate(new UpdateQueryImpl(query), sql.trim()).lockQuery();
			}
			case DELETE: {
				return compileDelete(query, sql.trim()).lockQuery();
			}
			default:
				throw new SQLException(String.format("Unable to compile query [%s], unknown query type", sql));
		}
	}

	private static Query compileDelete(QueryImpl query, String sql) throws SQLException {
		Matcher matcher = DELETE_PATTERN.matcher(sql);

		if (!matcher.matches()) {
			throw new SQLException(String.format("Invalid query [%s]", sql));
		}

		try {
			String templateName = matcher.group(TABLENAME_GROUP_NAME) + ResourceManagerFactory.DTYPE_SEPERATOR;
			String conditions[] = matcher.group(WHERE_CONDITIONS_GROUP_NAME).split("\\s+(and|AND)\\s+");
			Matcher conditionMatcher;

			for (String condition : conditions) {
				if ((conditionMatcher = WHERE_CONDITION_PATTERN.matcher(condition)).matches()) {
					if (conditionMatcher.group(STATEMENT_COLUMNNAME_GROUP_NAME)
							.equals(ResourceManagerFactory.DTYPE_COLUMNNAME)) {
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

	private static Query compileUpdate(UpdateQueryImpl query, String sql) throws SQLException {
		Matcher matcher = UPDATE_PATTERN.matcher(sql);

		if (!matcher.matches()) {
			throw new SQLException(String.format("Invalid query [%s]", sql));
		}

		try {
			String templateName = matcher.group(TABLENAME_GROUP_NAME) + ResourceManagerFactory.DTYPE_SEPERATOR;

			if (matcher.group(SET_STATEMENTS_GROUP_NAME) == null) {
				throw new SQLException(
						String.format("Query [%s] doesn't include any SET portion, expect at least 1", sql));
			}

			String[] parts = matcher.group(SET_STATEMENTS_GROUP_NAME).split("\\s?,\\s?");
			Matcher innerMatcher;

			for (String statement : parts) {
				if ((innerMatcher = SET_STATEMENT_PATTERN.matcher(statement)).matches()) {
					if (innerMatcher.group(STATEMENT_COLUMNNAME_GROUP_NAME)
							.equals(ResourceManagerFactory.DTYPE_COLUMNNAME)) {
						continue;
					}

					query.addColumnName(innerMatcher.group(STATEMENT_COLUMNNAME_GROUP_NAME));
					continue;
				}

				throw new SQLException(String.format("Invalid set statement pattern [%s]", statement));
			}

			if (matcher.group(WHERE_CONDITIONS_GROUP_NAME) == null) {
				throw new SQLException(
						String.format("Query [%s] doesn't include any WHERE portion, expect at least 1", sql));
			}

			parts = matcher.group(WHERE_CONDITIONS_GROUP_NAME).split("\\s+(and|AND)\\s+");

			for (String condition : parts) {
				if ((innerMatcher = WHERE_CONDITION_PATTERN.matcher(condition)).matches()) {
					if (innerMatcher.group(STATEMENT_COLUMNNAME_GROUP_NAME)
							.equals(ResourceManagerFactory.DTYPE_COLUMNNAME)) {
						templateName += innerMatcher.group(STATEMENT_VALUE_GROUP_NAME).replaceAll("'", "");
						continue;
					}

					query.addWhereStatementColumnName(innerMatcher.group(STATEMENT_COLUMNNAME_GROUP_NAME));
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
			String templateName = matcher.group(TABLENAME_GROUP_NAME) + ResourceManagerFactory.DTYPE_SEPERATOR;

			if (matcher.group(WHERE_CONDITIONS_GROUP_NAME) == null) {
				return query.setTemplateName(templateName);
			}

			String conditions[] = matcher.group(WHERE_CONDITIONS_GROUP_NAME).split("\\s+(and|AND)\\s+");
			Matcher conditionMatcher;

			for (String condition : conditions) {
				if ((conditionMatcher = WHERE_CONDITION_PATTERN.matcher(condition)).matches()) {
					if (conditionMatcher.group(STATEMENT_COLUMNNAME_GROUP_NAME)
							.equals(ResourceManagerFactory.DTYPE_COLUMNNAME)) {
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

		if (!matcher.matches()) {
			throw new SQLException(String.format("Invalid query [%s]", sql));
		}

		String templateName;

		try {
			templateName = matcher.group(TABLENAME_GROUP_NAME) + ResourceManagerFactory.DTYPE_SEPERATOR;

			String[] columnNames = matcher.group(COLUMNGROUP_GROUP_NAME).split("\\s?,\\s?");
			int span = columnNames.length;
			String columnName;

			for (int i = 0; i < span; i++) {
				columnName = columnNames[i];

				if (columnName.equals(ResourceManagerFactory.DTYPE_COLUMNNAME)) {
					// @formatter:off
					templateName += matcher.group(SET_STATEMENTS_GROUP_NAME)
							.split("\\s?,\\s?")[i]
									.split("'")[1];
					// @formatter:on
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
