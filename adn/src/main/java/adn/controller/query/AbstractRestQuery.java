/**
 * 
 */
package adn.controller.query;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractRestQuery {

	public AbstractRestQuery() {
		super();

//		Set<Field> thisFields = new HashSet<>(Arrays.asList(this.getClass().getDeclaredFields()));
//
//		this.getters = Stream.of(TypeHelper.getAllFields(getType())).filter(field -> !thisFields.contains(field))
//				.map(field -> PropertyAccessStrategyFactory.METHOD_ACCESS_STRATEGY
//						.buildPropertyAccess(getType(), field.getName(), field.getType()).getGetter())
//				.toArray(Getter[]::new);
	}

	public abstract boolean isEmpty();
}
