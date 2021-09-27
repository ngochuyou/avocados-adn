/**
 * 
 */
package adn.model.entities.metadata;

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

	public static final int CODE_LENGTH = _Category.CODE_LENGTH + 6 + 1; // 6 + delimiter
	public static final int MAXIMUM_MATERIAL_LENGTH = 50;
	
}
