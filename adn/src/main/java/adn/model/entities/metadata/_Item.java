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
public class _Item extends _PermanentEntity implements _AuditableResource {

	public static final String code = "code";

	public static final String namedSize = "namedSize";

	public static final String numericSize = "numericSize";

	public static final String color = "color";

	public static final String note = "note";

	public static final String status = "status";

	public static final String cost = "cost";

	public static final String product = "product";
	public static final String $product = "product_id";
	
	public static final String provider = "provider";
	public static final String $provider = "provider_id";

	public static final String orders = "orders";
	
	public static final String jnOrderDetails = "order_details";
	public static final String jnOrderDetailsId = "item_id";
	
	public static final String jnCart = "carts";
	public static final String jnCartId = "item_id";
		
	public static final String indexName = "active, color, namedSize, status";
	
	public static final int MAXIMUM_NAMED_SIZE_LENGTH = 4;
	public static final int MAXIMUM_NAMED_COLOR_LENGTH = 50;
	public static final int MAXIMUM_STATUS_LENGTH = 20;
	public static final int MAXIMUM_NOTE_LENGTH = 255;
	public static final int MAXIMUM_NUMERIC_SIZE_VALUE = 255; // UNSIGNED
	public static final int MINIMUM_NUMERIC_SIZE_VALUE = 1;

	public static final Pattern COLOR_PATTERN = Pattern
			.compile(String.format("^(([A-Za-z\\s]{1,%d})|(#(?:[0-9a-fA-F]{3,4}){1,2}))$", MAXIMUM_NAMED_COLOR_LENGTH));

	public static final Pattern NOTE_PATTERN = Pattern
			.compile(String.format("^[%s\\p{L}\\p{N}\s\n\\.\\_\\-\"\'!@#$%%&*,]{0,%d}$",
					StringHelper.VIETNAMESE_CHARACTERS, MAXIMUM_NOTE_LENGTH));

}
