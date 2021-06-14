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

	public static final String mark;
	public static final String letter;
	public static final String ops;

	public static Pattern SAVE_PATTERN;

	static {
		mark = "\\?,\\.''\"\\s\\t\\n><\\=\\(\\)";
		letter = "\\w\\d_";
		ops = "(\\=|like|LIKE|is|IS|>|<)";
		// @formatter:off
		String regex = String.format(""
				+ "(insert|INSERT)\\s+(into|INTO)\\s+(?<templatename>[%s]+)\\s+"
				+ "\\((?<columns>[%s]+)\\)\\s+"
				+ "(values|VALUES)\\s+\\((?<values>[%s]+)\\)\\s?",
				letter,
				letter + mark,
				letter + mark);
		// @formatter:on
		System.out.println(regex);
		SAVE_PATTERN = Pattern.compile(regex);
	}

	public static void main(String[] args) throws NoSuchMethodException, SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException, SQLException {
		String sql = "insert into ImageByBytes (createdDate, extension, lastModified, content, name, DTYPE) values (?, ?, ?, ?, ?, 'ImageByBytes')";
		Matcher matcher = SAVE_PATTERN.matcher(sql);

		if (matcher.matches()) {
			System.out.println(matcher.group("columns"));

			String[] values = matcher.group("values").split("\\s?,\\s?");

			for (String value : values) {
				System.out.println(value.startsWith("'") ? value.split("'")[1] : value);
			}
		} else {
			System.out.println("mismatch");
		}
	}

}
