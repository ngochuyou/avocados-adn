/**
 * 
 */
package adn.service.resource.engine;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.regex.Matcher;

import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.query.QueryCompiler;
import adn.service.resource.engine.query.QueryCompiler.QueryType;
import adn.service.resource.engine.template.ResourceTemplate;

/**
 * @author Ngoc Huy
 *
 */
public class MetadataFactory {

	public static final MetadataFactory INSTANCE = new MetadataFactory();

	private MetadataFactory() {}

	public ResultSetMetadataImplementor produce(Query query, ResourceTemplate template) throws SQLException {
		String[][] columnPairs = query.getType().equals(QueryType.FIND) ? getColumnNames(query.getActualSQLString())
				: new String[0][0];
		// @formatter:off
		return new ResultSetMetaDataImpl(
				template.getTemplateName(),
				columnPairs[1], columnPairs[0]);
		// @formatter:on
	}

	private String[][] getColumnNames(String sql) throws SQLException {
		Matcher matcher = QueryCompiler.SELECT_COLUMNS_PATTERN.matcher(sql);

		if (matcher.matches()) {
			String[] columnParts = matcher.group(QueryCompiler.COLUMNGROUP_GROUP_NAME).split(",\\s|,");
			String[][] columnPairs = new String[2][columnParts.length];
			Matcher columnMatcher;
			int i = 0;

			for (String column : columnParts) {
				if ((columnMatcher = QueryCompiler.SELECT_COLUMN_PATTERN.matcher(column)).matches()) {
					columnPairs[0][i] = columnMatcher.group(QueryCompiler.COLUMNGROUP_ACTUAL_NAME_GROUP_NAME);
					columnPairs[1][i] = columnMatcher.group(QueryCompiler.COLUMNGROUP_ALIAS_NAME_GROUP_NAME) != null
							? columnMatcher.group(QueryCompiler.COLUMNGROUP_ALIAS_NAME_GROUP_NAME)
							: columnMatcher.group(QueryCompiler.COLUMNGROUP_ACTUAL_NAME_GROUP_NAME);
					i++;
					continue;
				}

				throw new SQLException(String.format("Invalid column pattern [%s]", column));
			}

			return columnPairs;
		}

		throw new SQLException(String.format("Unable to produce %s from query [%s]", ResultSetMetaData.class, sql));
	}

}
