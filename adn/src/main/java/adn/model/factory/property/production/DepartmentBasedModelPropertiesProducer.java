/**
 * 
 */
package adn.model.factory.property.production;

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

	Map<String, Object> singularProduce(Object source, String columnName, UUID departmentId);

	List<Map<String, Object>> singularProduce(List<Object> sources, String columnName, UUID departmentId);

}
