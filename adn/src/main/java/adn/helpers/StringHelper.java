/**
 * 
 */
package adn.helpers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.util.StringUtils;

/**
 * @author Ngoc Huy
 *
 */
public class StringHelper extends StringUtils {

	public static final String VIETNAMESE_CHARACTERS = "ÁáÀàẢảÃãẠạĂăẮắẰằẲẳẴẵẶặÂâẤấẦầẨẩẪẫẬậĐđÉéÈèẺẻẼẽẸẹÊêỂểẾếỀềỄễỆệÍíÌìỊịỈỉĨĩỊịÓóÒòỎỏÕõỌọÔôỐốỒồỔổỖỗỘộƠơỚớỜờỞởỠỡỢợÚùÙùỦủŨũỤụƯưỨứỪừỬửỮữỰựÝýỲỳỶỷỸỹỴỵ";

	public static final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

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

	public static final String WHITESPACE_CHAR_CLASS = "[" + WHITESPACE_CHARS + "]";

	private static final MessageDigest SHA_256_MD;

	private static final SecureRandom RANDOM = new SecureRandom();

	static {
		MessageDigest digest;

		try {
			digest = MessageDigest.getInstance("SHA-256");

			byte[] salt = new byte[16];

			RANDOM.nextBytes(salt);
			digest.update(salt);
		} catch (NoSuchAlgorithmException nsae) {
			digest = null;
			nsae.printStackTrace();
			System.exit(-1);
		}

		SHA_256_MD = digest;
	}

	public static String hash(String input) {
		byte[] hashed = SHA_256_MD.digest(input.getBytes(StandardCharsets.UTF_8));

		StringBuilder sb = new StringBuilder();

		for (byte b : hashed)
			sb.append(String.format("%02x", b));

		return sb.toString();
	}

	public static boolean isLetters(String string) {
		return hasLength(string) && string.matches("^[\\p{L}]+$");
	}

	public static boolean isEmail(String email) {
		return hasLength(email) && email.matches(StringHelper.EMAIL_REGEX);
	}

	public static boolean isAcceptablePhoneNumber(String phoneNumber) {
		return hasLength(phoneNumber) && phoneNumber.matches("^[\\w\\d\\._\\(\\)\\+\\s\\-:]{4,}$");
	}

	public static boolean isBCrypt(String string) {
		return hasLength(string) && string.matches(StringHelper.BCRYPT_REGEX);
	}

	public static String normalizeString(String string) {
		return hasLength(string) ? string.trim().replaceAll(WHITESPACE_CHAR_CLASS + "+", " ") : string;
	}

	public static String removeSpaces(String string) {
		return hasLength(string) ? string.trim().replaceAll(WHITESPACE_CHAR_CLASS + "+", "") : string;
	}

	public static String toCamel(String s, CharSequence seperator) {
		String input = s.trim();

		if (seperator != null) {
			String[] parts = input.split(seperator.toString());

			if (parts.length > 1) {
				StringBuilder builder = new StringBuilder(
						("" + parts[0].charAt(0)).toLowerCase() + parts[0].substring(1, parts[0].length()));

				for (int i = 1; i < parts.length; i++) {
					builder.append(("" + parts[i].charAt(0)).toUpperCase() + parts[i].substring(1, parts[i].length()));
				}

				return builder.toString();
			}
		}

		return ("" + input.charAt(0)).toLowerCase() + input.substring(1);
	}

	public static String getFirstWord(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (("" + str.charAt(i)).matches(WHITESPACE_CHAR_CLASS)) {
				return str.substring(0, i);
			}
		}

		return str;
	}

	public static Optional<String> get(String in) {
		return Optional.ofNullable(hasLength(in) ? in : null);
	}
	
	public static Optional<String> get(String in, String append) {
		return Optional.ofNullable(hasLength(in) ? String.format("%s %s", in, append) : null);
	}

	public static String join(String... strings) {
		return Stream.of(strings).collect(Collectors.joining(", "));
	}
	
	public static String join(CharSequence joiner, String... strings) {
		return Stream.of(strings).collect(Collectors.joining(joiner));
	}

}
