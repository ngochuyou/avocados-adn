package adn.service;

import adn.model.Entity;

public interface ServiceExecutor {

	<T extends Entity> T execute(T instance, Class<T> clazz);

}
