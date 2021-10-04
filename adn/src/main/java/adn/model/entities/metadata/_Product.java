/**
 * 
 */
package adn.model.entities.metadata;

import java.util.regex.Pattern;

import adn.application.Common;
import adn.helpers.StringHelper;
import adn.service.resource.factory.DefaultResourceIdentifierGenerator;

/**
 * @author Ngoc Huy
 *
 */
public class _Product extends _FullyAuditedEntity {

	public static final String code = "code";

	public static final String material = "material";

	public static final String images = "images";

	public static final String description = "description";

	public static final String rating = "rating";

	public static final String locked = "locked";

	public static final String category = "category";

	public static final String items = "items";
	// _Category.CODE_LENGTH + DELIMITER
	public static final int MAXIMUM_CODE_LENGTH = 255;
	public static final int MAXIMUM_MATERIAL_LENGTH = 50;
	public static final int MAXIMUM_IMAGES_AMOUNT = 20;
	public static final int MAXIMUM_IMAGES_COLUMN_LENGTH = MAXIMUM_IMAGES_AMOUNT * DefaultResourceIdentifierGenerator.IDENTIFIER_LENGTH;
	// @formatter:off
	public static final Pattern MATERIAL_PATTERN = Pattern.compile(
			String.format(
					"^[%s\\p{L}\\p{N}\s/'\"\\-_]{0,%d}$",
					StringHelper.VIETNAMESE_CHARACTERS,
					MAXIMUM_MATERIAL_LENGTH));
	public static final Pattern DESCRIPTION_PATTERN = Pattern.compile(
			String.format(
					"^[%s\\p{L}\\p{N}\s\\.,()\\[\\]_\\-+=/\\\\!@#$%%^&*'\"?]{0,%d}$",
					StringHelper.VIETNAMESE_CHARACTERS, Common.MYSQL_TEXT_MAX_LENGTH));
	// @formatter:on
}
