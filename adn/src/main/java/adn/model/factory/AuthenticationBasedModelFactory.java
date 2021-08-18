package adn.model.factory;

import java.util.List;
import java.util.Map;

import adn.model.DomainEntity;
import adn.service.internal.Role;

public interface AuthenticationBasedModelFactory {

	<T extends DomainEntity> Map<String, Object> produce(Class<T> type, T entity);

	<T extends DomainEntity> Map<String, Object> produce(Class<T> type, T entity, Role role);

	<T extends DomainEntity> List<Map<String, Object>> produce(Class<T> type, List<T> entities);

	<T extends DomainEntity> List<Map<String, Object>> produce(Class<T> type, List<T> entities, Role role);

}
