/**
 * 
 */
package adn.dao.parameter;

import java.util.Collection;

/**
 * @author Ngoc Huy
 *
 */
public class ParamContext {

	private final ParamType type;
	private final Object value;

	public ParamContext(ParamType type, Object value) {
		super();
		this.type = type;
		this.value = value;
	}

	public ParamType getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}

	public static ParamContext singular(Object value) {
		return new ParamContext(ParamType.SINGULAR, value);
	}

	public static <T> ParamContext plural(Collection<T> collection) {
		return new ParamContext(ParamType.PLURAL, collection);
	}

	public static <T> ParamContext array(T[] array) {
		return new ParamContext(ParamType.ARRAY, array);
	}

}
