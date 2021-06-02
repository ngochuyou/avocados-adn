/**
 * 
 */
package adn.service.resource.engine.query;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.helpers.StringHelper;
import adn.service.resource.factory.ManagerFactory;

/**
 * @author Ngoc Huy
 *
 */
public final class QueryCompiler {

	public static enum QueryType {

		SAVE, FIND, DELETE, REGISTER_TEMPLATE, UNKNOWN;

		public static QueryType determineType(String sqlString) throws SQLException {
			String firstWord = StringHelper.getFirstWord(sqlString).toLowerCase();

			switch (firstWord) {
				case "insert": {
					return QueryType.SAVE;
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
					return QueryType.UNKNOWN;
				}
			}
		}

	}

	private QueryCompiler() {}

	private static final String INSERT_REGEX = "((?<=(insert\\sinto\\s))[\\w\\d_]+(?=\\s+))|((?<=\\()([\\w\\d_,\\s+]+)+(?=\\)))|values\\s+.+";
	private static final Pattern INSERT_PATTERN = Pattern.compile(INSERT_REGEX);

	private static final String SELECT_REGEX;
	private static final Pattern SELECT_PATTERN;

	private static final String PARAM_MEMBER_REGEX;
	private static final Pattern PARAM_MEMBER_PATTERN;

	private static final String TABLENAME_GROUP_NAME = "tablename";
	private static final String PARAMETERS_GROUP_NAME = "values";
	private static final String PARAM_NAME_GROUP_NAME = "name";
	private static final String PARAM_VALUE_GROUP_NAME = "value";

	static {
		String any = "\\w\\d\\?\\=\\,\\.\\s";
		String quotedAny = any + "''";
		String name = "\\w\\d_";
		// @formatter:off
		SELECT_REGEX = String.format(""
				+ "select[%s]+"
				+ "from\\s(?<%s>[%s]+)\\s[%s]+"
				+ "where\\s(?<%s>([%s]+\\.[%s]+\\=[%s]+)+)",
				any,
				TABLENAME_GROUP_NAME, name, any,
				PARAMETERS_GROUP_NAME, name, name, quotedAny);
		// @formatter:on
		SELECT_PATTERN = Pattern.compile(SELECT_REGEX);

		final Logger logger = LoggerFactory.getLogger(QueryCompiler.class);

		logger.trace(String.format("\n\tSELECT_REGEX:\n\t\t%s", SELECT_REGEX));
		logger.trace(String.format("\n\tINSERT_REGEX:\n\t\t%s", INSERT_REGEX));
		// @formatter:off
		PARAM_MEMBER_PATTERN = Pattern.compile(PARAM_MEMBER_REGEX =
				String.format("[%s]+\\.(?<%s>[%s]+)\\=(?<%s>[%s]+)", name, PARAM_NAME_GROUP_NAME, name, PARAM_VALUE_GROUP_NAME, quotedAny));
		// @formatter:on
		logger.trace(String.format("\n\tPARAM_MEMBER_REGEX:\n\t\t%s", PARAM_MEMBER_REGEX));
	}

	public static Query compile(Query other) throws SQLException {
		return new QueryImpl(other);
	}

	public static Query compile(String sql) throws SQLException {
		QueryImpl query = new QueryImpl().setQueryType(QueryType.determineType(sql));

		switch (query.getType()) {
			case FIND: {
				return compileFind(query, sql);
			}
			case SAVE: {
				return compileSave(query, sql);
			}
			default:
				throw new SQLException(String.format("Unable to compile query [%s], unknown query type", sql));
		}
	}

	private static Query compileFind(QueryImpl query, String sql) throws SQLException {
		Matcher matcher = SELECT_PATTERN.matcher(sql);

		if (!matcher.matches()) {
			throw new SQLException(String.format("Invalid query [%s]", sql));
		}

		try {
			String templateName = matcher.group(TABLENAME_GROUP_NAME) + ManagerFactory.DTYPE_SEPERATOR;
			String params[] = matcher.group(PARAMETERS_GROUP_NAME).split("\\sand\\s");
			Matcher paramMemberMatcher;

			for (String param : params) {
				if ((paramMemberMatcher = PARAM_MEMBER_PATTERN.matcher(param)).matches()) {
					if (paramMemberMatcher.group(PARAM_NAME_GROUP_NAME).equals(ManagerFactory.DTYPE_COLUMNNAME)) {
						templateName += paramMemberMatcher.group(PARAM_VALUE_GROUP_NAME).replaceAll("'", "");
						continue;
					}

					query.addColumnName(paramMemberMatcher.group(PARAM_NAME_GROUP_NAME));
					continue;
				}

				throw new SQLException(String.format("Invalid param pattern [%s]", param));
			}

			return query.setTemplateName(templateName).lockQuery();
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

			return query.setTemplateName(templateName).lockQuery();
		} catch (RuntimeException any) {
			throw new SQLException(any);
		}
	}

}
