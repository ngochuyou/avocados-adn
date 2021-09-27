/**
 * 
 */
package adn.model.entities.metadata;

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

	public static final String provider = "provider";

	public static final String price = "price";

	public static final int IDENTIFIER_LENGTH = _Product.CODE_LENGTH + 8 + 1; // 8 + delimiter
	public static final int MAXIMUM_NAMED_SIZE_LENGTH = 4;
	public static final int MAXIMUM_NAMED_COLOR_LENGTH = 50;
	public static final int MAXIMUM_STATUS_LENGTH = 20;
	public static final int MAXIMUM_DESCRIPTION_LENGTH = 255;
	public static final int MAXIMUM_NUMERIC_SIZE_VALUE = 255; // UNSIGNED
	public static final int MINIMUM_NUMERIC_SIZE_VALUE = 1;

}
