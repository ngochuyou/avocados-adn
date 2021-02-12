/**
 * 
 */
package adn.utilities;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.springframework.util.StringUtils;

import adn.application.Constants;

/**
 * @author Ngoc Huy
 *
 */
public class Strings extends StringUtils {

	public static String hash(String a) {
		MessageDigest md;

		try {
			// Select the message digest for the hash computation -> SHA-256
			md = MessageDigest.getInstance("SHA-256");
			// Generate the random salt
			SecureRandom random = new SecureRandom();
			byte[] salt = new byte[16];
			random.nextBytes(salt);
			// Passing the salt to the digest for the computation
			md.update(salt);
			// Generate the salted hash
			byte[] hashedPassword = md.digest(a.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();

			for (byte b : hashedPassword)
				sb.append(String.format("%02x", b));

			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "shjreisSnaIo";
		}
	}

	public static boolean isEmail(String email) {

		return email == null ? false : email.matches(Constants.EMAIL_REGEX);
	}

	public static boolean isDigits(String string) {

		return string == null ? false : string.matches("\\d+");
	}

	public static boolean isBCrypt(String string) {

		return string == null ? false : string.matches(Constants.BCRYPT_REGEX);
	}

	public static String normalizeString(String string) {

		return string != null ? string.trim().replaceAll("[" + Constants.WHITESPACE_CHARS + "]+", " ") : null;
	}

	public static String removeSpaces(String string) {

		return string != null ? string.trim().replaceAll("[" + Constants.WHITESPACE_CHARS + "]+", "") : null;
	}

	public static String toCamel(String s, CharSequence seperator) {
		if (seperator != null) {
			String[] parts = s.split(seperator + "+");

			if (parts.length > 1) {
				StringBuilder builder = new StringBuilder(
						("" + parts[0].charAt(0)).toLowerCase() + parts[0].substring(1, parts[0].length()));

				for (int i = 1; i < parts.length; i++) {
					builder.append(("" + parts[i].charAt(0)).toUpperCase()
							+ parts[i].substring(1, parts[i].length()));
				}

				return builder.toString();
			}
		}

		return ("" + s.charAt(0)).toLowerCase() + s.substring(1);
	}

	public static String removeFirstCamelWord(String s) {
		if (s == null || !Strings.hasLength(s)) {
			return "";
		}

		String result = Strings.removeSpaces(s);
		int length = result.length();

		for (int i = 0; i < length; i++) {
			if (result.charAt(i) >= 'A' && result.charAt(i) <= 'Z') {
				return ("" + result.charAt(i)).toLowerCase() + result.substring(i + 1);
			}
		}

		return result;
	}

}
