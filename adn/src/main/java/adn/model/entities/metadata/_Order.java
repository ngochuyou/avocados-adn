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
public class _Order extends _PermanentEntity {

	public static final String code = "code";

	public static final String status = "status";

	public static final String address = "address";

	public static final String district = "district";

	public static final String deliveryFee = "deliveryFee";

	public static final String createdTimestamp = "createdTimestamp";

	public static final String customer = "customer";

	public static final String updatedTimestamp = "updatedTimestamp";

	public static final String note = "note";

	public static final String items = "items";

	public static final String jnOrderDetails = "order_details";
	public static final String jnOrderDetailsId = "order_id";

	public static final Pattern ADDRESS_PATTERN;
	public static final Pattern NOTE_PATTERN;
	
	public static final int MAXIMUM_ADDRESS_LENGTH = 255;
	public static final int MAXIMUM_NOTE_LENGTH = 500;

	static {
		ADDRESS_PATTERN = Pattern.compile(String.format("^[%s\\p{L}\\p{N}\s_\\-.,*'\"/&]{0,%d}$",
				StringHelper.VIETNAMESE_CHARACTERS, MAXIMUM_ADDRESS_LENGTH));
		NOTE_PATTERN = Pattern.compile(String.format("^[%s\\p{L}\\p{N}\s_\\-.,*'\"/&]{0,%d}$",
				StringHelper.VIETNAMESE_CHARACTERS, MAXIMUM_NOTE_LENGTH));
	}

}
