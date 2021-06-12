/**
 * 
 */
package adn.helpers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.util.StringUtils;

import adn.application.context.ContextProvider;

/**
 * @author Ngoc Huy
 *
 */
public class StringHelper extends StringUtils {

	// stolen from stackoverflow below
	public static final String EMAIL_REGEX = "(?p:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

	public static final String BCRYPT_REGEX = "^\\$2[ayb]\\$.{56}$";

	public static final String WHITESPACE_CHARS = "" /* dummy empty string for homogeneity */
			+ "\\u0009" // CHARACTER TABULATION
			+ "\\u000A" // LINE FEED (LF)
			+ "\\u000B" // LINE TABULATION
			+ "\\u000C" // FORM FEED (FF)
			+ "\\u000D" // CARRIAGE RETURN (CR)
			+ "\\u0020" // SPACE
			+ "\\u0085" // NEXT LINE (NEL)4
			+ "\\u00A0" // NO-BREAK SPACE
			+ "\\u1680" // OGHAM SPACE MARK
			+ "\\u180E" // MONGOLIAN VOWEL SEPARATOR
			+ "\\u2000" // EN QUAD
			+ "\\u2001" // EM QUAD
			+ "\\u2002" // EN SPACE
			+ "\\u2003" // EM SPACE
			+ "\\u2004" // THREE-PER-EM SPACE
			+ "\\u2005" // FOUR-PER-EM SPACE
			+ "\\u2006" // SIX-PER-EM SPACE
			+ "\\u2007" // FIGURE SPACE
			+ "\\u2008" // PUNCTUATION SPACE
			+ "\\u2009" // THIN SPACE
			+ "\\u200A" // HAIR SPACE
			+ "\\u2028" // LINE SEPARATOR
			+ "\\u2029" // PARAGRAPH SEPARATOR
			+ "\\u202F" // NARROW NO-BREAK SPACE
			+ "\\u205F" // MEDIUM MATHEMATICAL SPACE
			+ "\\u3000"; // IDEOGRAPHIC SPACE

	public static final String ONE_OF_WHITESPACE_CHARS = "[" + WHITESPACE_CHARS + "]";

	private static MessageDigest SHA_256_MD = null;

	private static SecureRandom random = new SecureRandom();

	static {
		try {
			SHA_256_MD = MessageDigest.getInstance("SHA-256");

			byte[] salt = new byte[16];

			random.nextBytes(salt);
			SHA_256_MD.update(salt);
		} catch (NoSuchAlgorithmException nsae) {
			nsae.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}
	}

	public static String hash(String input) {
		byte[] hashedPassword = SHA_256_MD.digest(input.getBytes(StandardCharsets.UTF_8));

		StringBuilder sb = new StringBuilder();

		for (byte b : hashedPassword)
			sb.append(String.format("%02x", b));

		return sb.toString();
	}

	public static boolean isEmail(String email) {

		return email == null ? false : email.matches(StringHelper.EMAIL_REGEX);
	}

	public static boolean isDigits(String string) {

		return string == null ? false : string.matches("\\d+");
	}

	public static boolean isBCrypt(String string) {

		return string == null ? false : string.matches(StringHelper.BCRYPT_REGEX);
	}

	public static String normalizeString(String string) {
		return string != null ? string.trim().replaceAll(ONE_OF_WHITESPACE_CHARS + "+", " ") : null;
	}

	public static String removeSpaces(String string) {
		return string != null ? string.trim().replaceAll(ONE_OF_WHITESPACE_CHARS + "+", "") : null;
	}

	public static String toCamel(String s, CharSequence seperator) {
		if (seperator != null) {
			String[] parts = s.split(seperator.toString());

			if (parts.length > 1) {
				StringBuilder builder = new StringBuilder(
						("" + parts[0].charAt(0)).toLowerCase() + parts[0].substring(1, parts[0].length()));

				for (int i = 1; i < parts.length; i++) {
					builder.append(("" + parts[i].charAt(0)).toUpperCase() + parts[i].substring(1, parts[i].length()));
				}

				return builder.toString();
			}
		}

		return ("" + s.charAt(0)).toLowerCase() + s.substring(1);
	}

	public static String getFirstWord(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (("" + str.charAt(i)).matches(ONE_OF_WHITESPACE_CHARS)) {
				return str.substring(0, i);
			}
		}

		return str;
	}

	public static Optional<String> get(String in) {
		return Optional.ofNullable(hasLength(in) ? in : null);
	}

}
