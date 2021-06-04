/**
 * 
 */
package adn.test.application;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import adn.helpers.StringHelper;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.query.QueryCompiler;

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
		String sql = "update ImageByBytes set createdDate=?, extension=?, lastModified=?, content=? where name=? and lastModified=?";
		String mark = "\\?,\\.''\"\\s\\t\\n><\\=\\(\\)";
		String letter = "\\w\\d_";
		String ops = "(\\=|like|LIKE|is|IS|>|<)";
		// @formatter:off
		Pattern p = Pattern.compile(
			String.format(""
				+ "update\\s+(?<tablename>[%s]+)\\s+"
				+ "set\\s+(?<columns>[%s]+)\\s+"
				+ "where\\s+(?<conditions>[%s]+)",
				letter,
				mark + letter,
				letter + mark + ops)
		);
		// @formatter:on
		Matcher m = p.matcher(sql);

		if (m.matches()) {
			System.out.println(m.group("tablename"));

			String[] columnParts = StringHelper.removeSpaces(m.group("columns")).split(",");

			for (String s : columnParts) {
				// @formatter:off
				Matcher columnMatcher = Pattern.compile(String.format(""
					+ "(?<columnname>[%s]+)[%s]+",
					letter, mark)
				).matcher(s);
				// @formatter:on
				if (columnMatcher.matches()) {
					System.out.println(columnMatcher.group("columnname"));
				}
			}

			System.out.println(m.group("conditions"));
		}
	}

	@Test
	public void testCompileFind() throws SQLException {
		// @formatter:off
		String sql = ""
				+ "select imagebybyt0_.name as name1_0_0_, imagebybyt0_.createdDate as createdd2_0_0_, imagebybyt0_.extension as extensio3_0_0_, imagebybyt0_.lastModified as lastmodi4_0_0_, imagebybyt0_.content as content5_0_0_ "
				+ "from ImageByBytes imagebybyt0_ "
				+ "where imagebybyt0_.name=? ";
		// @formatter:on
		System.out.println(sql);
		Query query = QueryCompiler.compile(sql, null);
		System.out.println(query);
	}

}
