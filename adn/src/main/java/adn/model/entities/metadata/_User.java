/**
 * 
 */
package adn.model.entities.metadata;

import java.util.regex.Pattern;

import adn.helpers.StringHelper;

/**
 * @author Ngoc Huy
 *
 */
public class _User extends _PermanentEntity {

	public static final String email = "email";

	public static final String address = "address";

	public static final String phone = "phone";

	public static final String lastName = "lastName";

	public static final String firstName = "firstName";

	public static final String gender = "gender";

	public static final String birthDate = "birthDate";

	public static final String photo = "photo";

	public static final String role = "role";

	public static final String password = "password";

	public static final String updatedDate = "updatedDate";

	public static final String locked = "locked";
	
	public static final String _id = "username";

	public static final Pattern USERNAME_PATTERN;
	public static final Pattern NAME_PATTERN;
	public static final int MINIMUM_USERNAME_LENGTH = 8;
	public static final int MAXIMUM_USERNAME_LENGTH = 8;
	public static final int MINIMUM_NAME_LENGTH = 1;
	public static final int MAXIMUM_NAME_LENGTH = 255;
	public static final int MINIMUM_PASSWORD_LENGTH = 8;
	public static final int PASSWORD_MAX_LENGTH = 60;
	
	static {
		USERNAME_PATTERN = Pattern.compile(String.format("^[\\p{L}\\p{N}\\._@#$%-'+=><]{%d,%d}$",
				MINIMUM_USERNAME_LENGTH, MAXIMUM_USERNAME_LENGTH));
		NAME_PATTERN = Pattern.compile(String.format("^[%s\\p{L}\\p{N}\\.\\-'()\s]{%d,%d}$",
				StringHelper.VIETNAMESE_CHARACTERS, MINIMUM_NAME_LENGTH, MAXIMUM_NAME_LENGTH));
	}

}
