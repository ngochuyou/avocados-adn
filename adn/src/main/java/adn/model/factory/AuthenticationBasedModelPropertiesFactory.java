/**
 * 
 */
package adn.model.factory;

import java.util.List;
import java.util.Map;

import adn.model.AbstractModel;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public interface AuthenticationBasedModelPropertiesFactory {

	<T extends AbstractModel> Map<String, Object> produce(Class<T> type, Map<String, Object> properties);

	<T extends AbstractModel> Map<String, Object> produce(Class<T> type, Map<String, Object> properties, Role role);

	<T extends AbstractModel> List<Map<String, Object>> produce(Class<T> type, List<Map<String, Object>> properties);

	<T extends AbstractModel> List<Map<String, Object>> produce(Class<T> type, List<Map<String, Object>> properties,
			Role role);

	<T extends AbstractModel> Map<String, Object> produceImmutable(Class<T> type, Map<String, Object> properties);

	<T extends AbstractModel> Map<String, Object> produceImmutable(Class<T> type, Map<String, Object> properties,
			Role role);

	<T extends AbstractModel> List<Map<String, Object>> produceImmutable(Class<T> type,
			List<Map<String, Object>> properties);

	<T extends AbstractModel> List<Map<String, Object>> produceImmutable(Class<T> type,
			List<Map<String, Object>> properties, Role role);

}
