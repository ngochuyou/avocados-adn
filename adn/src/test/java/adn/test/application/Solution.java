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
		String directory = "C:\\Users\\Ngoc Huy\\Documents\\avocados-adn\\";
		String sub = "image\\";
		String pat;
		Pattern p = Pattern
				.compile(pat = String.format("^(?<dir>%s)(?<mid>([\\w\\d_-]+(\\\\)?)+)(?<extension>\\.[\\w\\d]+)$",
						Pattern.quote(directory + sub)));
		String filename = "C:\\Users\\Ngoc Huy\\Documents\\avocados-adn\\image\\123asd\\asdasd.jpg";
		Matcher m = p.matcher(filename);

		System.out.println(pat);

		if (m.matches()) {
			System.out.println(m.replaceAll("${mid}${extension}"));
		}
	}

	
}
