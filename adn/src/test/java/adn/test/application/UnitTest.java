/**
 * 
 */
package adn.test.application;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import adn.helpers.TypeHelper;
import adn.model.AbstractModel;
import adn.model.entities.Account;
import adn.model.entities.Entity;
import adn.model.entities.Personnel;

/**
 * @author Ngoc Huy
 *
 */
public class UnitTest {

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
			IllegalArgumentException, IllegalAccessException {

		Stream.of(Entity.class, AbstractModel.class, Entity.class, AbstractModel.class, Personnel.class, Account.class,
				Account.class).sorted((o, t) -> {
					return o.equals(t) ? 0 : (TypeHelper.isExtendedFrom(o, t) ? 1 : -1);
				}).forEach(type -> {
					System.out.println(type.getName());
				});
	}

	public static <T extends Account> List<T> test(List<T> arr) {
		return arr;
	}

	public static void testUnicodePattern() {
		Pattern p = Pattern.compile("^[_\\p{L}\\p{N}\\.]{8,}$", Pattern.UNICODE_CHARACTER_CLASS);
		Matcher m = p.matcher("孔子及其弟asdads子故事集_真.实性有争议_");

		System.out.println(m.matches());
	}

}
