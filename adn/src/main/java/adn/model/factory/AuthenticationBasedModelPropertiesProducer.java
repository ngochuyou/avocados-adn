/**
 * 
 */
package adn.model.factory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import adn.model.factory.property.production.ModelPropertiesProducer;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public interface AuthenticationBasedModelPropertiesProducer extends ModelPropertiesProducer {

	@Override
	default Map<String, Object> produce(Map<String, Object> source) {
		return produce(source, null);
	}

	Map<String, Object> produce(Map<String, Object> source, Role role);

	default Map<String, Object> produceImmutable(Map<String, Object> source, Role role) {
		return Collections.unmodifiableMap(produce(source, role));
	}

	@Override
	default List<Map<String, Object>> produce(List<Map<String, Object>> source) {
		return produce(source, null);
	}

	List<Map<String, Object>> produce(List<Map<String, Object>> source, Role role);

	default List<Map<String, Object>> produceImmutable(List<Map<String, Object>> source, Role role) {
		return Collections.unmodifiableList(produce(source, role));
	}

}
