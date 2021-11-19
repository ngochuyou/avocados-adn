/**
 * 
 */
package adn.model.entities.metadata;

/**
 * @author Ngoc Huy
 *
 */
public class _OrderDetail extends _PermanentEntity {

	public static final String order = "order";

	public static final String item = "item";

	public static final String orderId = "orderId";
	public static final String $orderId = "order_id";

	public static final String itemId = "itemId";
	public static final String $itemId = "item_id";

	public static final String rating = "rating";

	public static final String price = "price";

	public static final String indexName = "active, item_id, order_id";
	
}
