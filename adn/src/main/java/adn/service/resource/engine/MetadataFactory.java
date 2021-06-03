/**
 * 
 */
package adn.service.resource.engine;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.template.ResourceTemplate;

/**
 * @author Ngoc Huy
 *
 */
public class MetadataFactory {

	public static final MetadataFactory INSTANCE = new MetadataFactory();

	private static final String COLUMNS_GROUP_NAME = "columns";
	private static final Pattern COLUMNS_PATTERN;
	private static final String ACTUAL_COLUMN_NAME_GROUP_NAME = "actualcolumnname";
	private static final String COLUMN_ALIAS_GROUP_NAME = "columnalias";
	private static final Pattern COLUMN_PATTERN;
	// @formatter:off
	static {
		String any = "\\w\\d\\?\\=\\,\\.\\s_";
		String quotedAny = any + "''";
		String name = "\\w\\d_";
		
		COLUMNS_PATTERN = Pattern.compile(
			String.format(
				"select\\s(?<%s>[%s]+)\\sfrom[%s]+",
				COLUMNS_GROUP_NAME, any, quotedAny)
		);
		COLUMN_PATTERN = Pattern.compile(
			String.format(""
				+ "^[%s]+"
				+ "\\."
				+ "(?<%s>[%s]+)"
				+ "(\\sas\\s"
				+ "(?<%s>[%s]+))?",
				name,
				ACTUAL_COLUMN_NAME_GROUP_NAME, name,
				COLUMN_ALIAS_GROUP_NAME, name)
		);
	}
	// @formatter:on
	private MetadataFactory() {}

	public ResultSetMetadataImplementor produce(Query query, ResourceTemplate template) throws SQLException {
		String[][] columnPairs = getColumnNames(query.getActualSQLString());
		// @formatter:off
		return new ResultSetMetaDataImpl(
				template.getName(),
				columnPairs[1], columnPairs[0]);
		// @formatter:on
	}

	private String[][] getColumnNames(String sql) throws SQLException {
		Matcher matcher = COLUMNS_PATTERN.matcher(sql);

		if (matcher.matches()) {
			String[] columnParts = matcher.group(COLUMNS_GROUP_NAME).split(",\\s|,");
			String[][] columnPairs = new String[2][columnParts.length];
			Matcher columnMatcher;
			int i = 0;

			for (String column : columnParts) {
				if ((columnMatcher = COLUMN_PATTERN.matcher(column)).matches()) {
					columnPairs[0][i] = columnMatcher.group(ACTUAL_COLUMN_NAME_GROUP_NAME);
					columnPairs[1][i] = columnMatcher.group(COLUMN_ALIAS_GROUP_NAME) != null
							? columnMatcher.group(COLUMN_ALIAS_GROUP_NAME)
							: columnMatcher.group(ACTUAL_COLUMN_NAME_GROUP_NAME);
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
