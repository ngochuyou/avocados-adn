/**
 * 
 */
package adn.utilities;

import org.springframework.util.StringUtils;

import adn.application.Constants;

/**
 * @author Ngoc Huy
 *
 */
public class Strings extends StringUtils {

	public static boolean isEmail(String email) {

		return email == null ? false : email.matches(Constants.EMAIL_REGEX);
	}

	public static boolean isDigits(String string) {

		return string == null ? false : string.matches("\\d+");
	}

	public static boolean isBCrypt(String string) {

		return string == null ? false : string.matches(Constants.BCRYPT_REGEX);
	}

}
