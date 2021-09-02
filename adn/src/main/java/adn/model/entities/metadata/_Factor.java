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
public class _Factor extends _PermanentEntity {

	public static final String name = "name";

	public static final String createdBy = "createdBy";

	public static final String createdTimestamp = "createdTimestamp";

	public static final String updatedBy = "updatedBy";

	public static final String updatedTimestamp = "updatedTimestamp";

	public static final String deactivatedTimestamp = "deactivatedTimestamp";

	public static final String approvedBy = "approvedBy";

	public static final String approvedTimestamp = "approvedTimestamp";

	public static final int MINIMUM_NAME_LENGTH = 1;
	public static final int MAXIMUM_NAME_LENGTH = 255;
	public static final Pattern NAME_PATTERN;

	static {
		NAME_PATTERN = Pattern.compile(String.format("^[\\p{L}\\p{N}\\s\\.,_\\-@\"\'%%\\*%s]+$",
				StringHelper.VIETNAMESE_CHARACTERS, MINIMUM_NAME_LENGTH, MAXIMUM_NAME_LENGTH));
	}

}
