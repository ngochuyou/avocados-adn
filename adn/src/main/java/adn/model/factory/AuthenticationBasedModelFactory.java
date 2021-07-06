package adn.model.factory;

import java.util.List;
import java.util.Map;

import adn.model.AbstractModel;
import adn.service.internal.Role;

public interface AuthenticationBasedModelFactory {

	<T extends AbstractModel> Map<String, Object> produce(Class<T> type, T entity);

	<T extends AbstractModel> Map<String, Object> produce(Class<T> type, T entity, Role role);

	<T extends AbstractModel> List<Map<String, Object>> produce(Class<T> type, List<T> entities);

	<T extends AbstractModel> List<Map<String, Object>> produce(Class<T> type, List<T> entities, Role role);

}
