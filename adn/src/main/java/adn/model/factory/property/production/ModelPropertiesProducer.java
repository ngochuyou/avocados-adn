/**
 * 
 */
package adn.model.factory.property.production;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import adn.model.factory.ModelProducer;

/**
 * @author Ngoc Huy
 *
 */
public interface ModelPropertiesProducer extends ModelProducer<Object[], Map<String, Object>> {

	public static final Function<Object, Object> MASKER = val -> null;
	public static final Function<Object, Object> PUBLISHER = val -> val;

	@Override
	Map<String, Object> produce(Object[] source);

	@Override
	default Map<String, Object> produceImmutable(Object[] source) {
		return Collections.unmodifiableMap(produce(source));
	}

	@Override
	List<Map<String, Object>> produce(List<Object[]> source);

	@Override
	default List<Map<String, Object>> produceImmutable(List<Object[]> source) {
		return Collections.unmodifiableList(produce(source));
	}

}
