/**
 * 
 */
package adn.test.application;

import java.sql.SQLException;
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
		SAVE_PATTERN = Pattern.compile(regex);
	}

	public static void main(String[] args) throws NoSuchMethodException, SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException, SQLException {

	}

}
