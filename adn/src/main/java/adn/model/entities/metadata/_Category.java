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
public class _Category extends _PermanentEntity implements _NamedResource {

	public static final String code = "code";

	public static final String description = "description";

	public static final String products = "products";

	public static final int MAXIMUM_CODE_LENGTH = 5;
	public static final int MAX_DESCRIPTION_LENGTH = 255;
	// @formatter:off
	public static final Pattern DESCRIPTION_PATTERN = Pattern.compile(
			String.format("^[%s\\p{L}\\p{N}\s\\.,()\\[\\]_\\-+=/\\\\!@#$%%^&*'\"?]{0,%d}$",
					StringHelper.VIETNAMESE_CHARACTERS,
					MAX_DESCRIPTION_LENGTH));
	// @formatter:on
}
