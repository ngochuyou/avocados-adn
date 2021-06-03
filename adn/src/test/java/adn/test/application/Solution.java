/**
 * 
 */
package adn.test.application;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ngoc Huy
 *
 */
public class Solution {

	/**
	 * @param args
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SQLException
	 */
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException, SQLException {
		String any = "\\w\\d\\?\\=\\,\\.\\s\\_";
		String quotedAny = any + "''";
		String name = "\\w\\d_";
		// @formatter:off
		String sql = ""
				+ "select imagebybyt0_.name as name2_0_0_, imagebybyt0_.createdDate as createdd3_0_0_, imagebybyt0_.extension as extensio4_0_0_, imagebybyt0_.lastModified as lastmodi5_0_0_, imagebybyt0_.content "
				+ "from FileResource imagebybyt0_ "
				+ "where imagebybyt0_.name=? and imagebybyt0_.DTYPE='ImageByBytes'";
		Pattern p = Pattern.compile(
				String.format("select\\s(?<columns>[%s]+)\\sfrom[%s]+", any, quotedAny)
		);
		// @formatter:on
		Matcher m = p.matcher(sql);

		if (m.matches()) {
			String[] columns = m.group("columns").split(",\\s|,");
			// @formatter:off
			Pattern columnPattern = Pattern.compile(
				String.format(""
					+ "^(?<tablename>[%s]+)"
					+ "\\."
					+ "(?<actualcolumnname>[%s]+)"
					+ "(\\sas\\s"
					+ "(?<columnalias>[%s]+))?",
					name, name, name)
			);
			// @formatter:on
			Matcher columnMatcher;
			for (String column : columns) {
				System.out.println("-----origi" + column);
				(columnMatcher = columnPattern.matcher(column)).matches();
				System.out.println(columnMatcher.group("actualcolumnname"));
				System.out.println(columnMatcher .group("columnalias"));
			}
		}
	}

}
