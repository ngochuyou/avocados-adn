/**
 * 
 */
package adn.model.entities.metadata;

/**
 * @author Ngoc Huy
 *
 */
public class _Product extends _Factor {

	public static final String price = "price";

	public static final String category = "category";

	public static final String images = "images";

	public static final String description = "description";

	public static final String rating = "rating";

	public static final String stockDetails = "stockDetails";

	public static final String ID_COLUMN_DEFINITION = "VARCHAR(11)";
	public static final int ID_LENGTH = _Category.IDENTIFIER_LENGTH + 5 + 1; // 5 + delimiter

}
