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
public interface AuthenticationBasedModelPropertiesProducer
		extends ModelPropertiesProducer, AuthenticationBasedModelProducer<Object[], Map<String, Object>> {

	@Override
	default Map<String, Object> produce(Object[] source) {
		return produce(source, null);
	}

	Map<String, Object> produce(Object[] source, Role role);

	default Map<String, Object> produceImmutable(Object[] source, Role role) {
		return Collections.unmodifiableMap(produce(source, role));
	}

	@Override
	default List<Map<String, Object>> produce(List<Object[]> source) {
		return produce(source, null);
	}

	List<Map<String, Object>> produce(List<Object[]> source, Role role);

	default List<Map<String, Object>> produceImmutable(List<Object[]> source, Role role) {
		return Collections.unmodifiableList(produce(source, role));
	}

	Map<String, Object> produce(Object[] source, Role role, String[] columnNames);

	default Map<String, Object> produceImmutable(Object[] source, Role role, String[] columnNames) {
		return Collections.unmodifiableMap(produce(source, role, columnNames));
	}

	List<Map<String, Object>> produce(List<Object[]> source, Role role, String[] columnNames);

}
