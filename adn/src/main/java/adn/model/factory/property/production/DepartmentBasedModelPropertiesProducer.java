/**
 * 
 */
package adn.model.factory.property.production;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import adn.model.factory.DepartmentBasedModelProducer;

/**
 * @author Ngoc Huy
 *
 */
public interface DepartmentBasedModelPropertiesProducer
		extends DepartmentBasedModelProducer<Object[], Map<String, Object>>, ModelPropertiesProducer {

	Map<String, Object> produce(Object[] source, String[] columns, UUID departmentId);

	List<Map<String, Object>> produce(List<Object[]> sources, String[] columns, UUID departmentId);

	@Override
	default List<Map<String, Object>> produceImmutable(List<Object[]> sources) {
		return Collections.unmodifiableList(produce(sources));
	}

	@Override
	default Map<String, Object> produceImmutable(Object[] source) {
		return Collections.unmodifiableMap(produce(source));
	}

}
