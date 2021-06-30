package adn.model.factory;

import java.util.Map;

import adn.application.context.ContextBuilder;
import adn.model.AbstractModel;
import adn.service.internal.Role;

public interface AuthenticationBasedModelFactory extends ContextBuilder {

	<T extends AbstractModel> Map<String, Object> produce(Class<T> type, T entity);

	<T extends AbstractModel> Map<String, Object> produce(Class<T> type, T entity, Role role);

}
