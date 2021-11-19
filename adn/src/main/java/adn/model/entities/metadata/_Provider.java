/**
 * 
 */
package adn.model.entities.metadata;

import static adn.helpers.StringHelper.VIETNAMESE_CHARACTERS;

import java.util.regex.Pattern;

/**
 * @author Ngoc Huy
 *
 */
public class _Provider extends _PermanentEntity implements _NamedResource {

	public static final String address = "address";

	public static final String website = "website";

	public static final String email = "email";

	public static final String phoneNumbers = "phoneNumbers";

	public static final String representatorName = "representatorName";

	public static final String productCosts = "productCosts";

	public static final int MAXIMUM_ADDRESS_LENGTH = 255;
	public static final int MINIMUM_ADDRESS_LENGTH = 1;
	public static final int MAXIMUM_REPRESENTATOR_NAME_LENGTH = 255;
	// @formatter:off
	public static final Pattern ADDRESS_PATTERN = Pattern
			.compile(String.format("^[%s\\p{L}\\p{N}\s\\.,\\-_()*/@\\\\]{%d,%d}$",
					VIETNAMESE_CHARACTERS,
					MINIMUM_ADDRESS_LENGTH, MAXIMUM_ADDRESS_LENGTH));
	// @formatter:on
	public static final int WEBSITE_MAX_LENGTH = 2000;
	public static final int PHONENUMBERS_MAX_LENGTH = 255;
	public static final Pattern REPRESENTATOR_NAME_PATTERN = Pattern.compile(String
			.format("^[%s\\p{L}\\p{N}\s\\.,_\\-'\"()]{0,%d}$", VIETNAMESE_CHARACTERS, MAXIMUM_REPRESENTATOR_NAME_LENGTH));
}
