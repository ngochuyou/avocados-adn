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
//		String SELECT_REGEX = "(select[\\w\\d\\?\\=\\,\\.\\s]+)(from[\\w\\d\\?\\=\\,\\.\\s]+)(where[\\w\\d\\?\\=\\,\\.\\s\'\']+)";
		String any = "\\w\\d\\?\\=\\,\\.\\s";
		String quotedAny = any + "''";
		String name = "\\w\\d_";
		// @formatter:off
		String sql = ""
			+ "select imagebybyt0_.name as name2_0_0_, imagebybyt0_.createdDate as createdd3_0_0_, imagebybyt0_.extension as extensio4_0_0_, imagebybyt0_.lastModified as lastmodi5_0_0_, imagebybyt0_.content as content6_0_0_ "
			+ "from FileResource imagebybyt0_ "
			+ "where imagebybyt0_.name=? and imagebybyt0_.DTYPE='ImageByBytes'";
		String SELECT_REGEX = String.format(""
				+ "select[%s]+"
				+ "from\\s(?<tablename>[%s]+)\\s[%s]+"
				+ "where\\s(?<values>([%s]+\\.[%s]+\\=[%s]+)+)", any, name,
				any, name, name, quotedAny);
		// @formatter:on
		Pattern p = Pattern.compile(SELECT_REGEX);
		Matcher m = p.matcher(sql);

		if (m.matches()) {
			String templateName = null;
			String vPS;
			String values[] = m.group("values").split("\\sand\\s");
			Pattern vP = Pattern
					.compile(vPS = String.format("[%s]+\\.(?<name>[%s]+)\\=(?<val>[%s]+)", name, name, quotedAny));
			Matcher vM;

			System.out.println(vPS);

			for (String value : values) {
				if ((vM = vP.matcher(value)).matches()) {
					if (vM.group("name").equals("DTYPE")) {
						templateName = m.group("tablename") + '_' + vM.group("val").replaceAll("'", "");
						continue;
					}
				}
			}

			System.out.println(templateName);
		}
	}

}
