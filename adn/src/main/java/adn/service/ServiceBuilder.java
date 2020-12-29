package adn.service;

import adn.application.context.ContextProvider;
import adn.utilities.TypeUtils;

public interface ServiceBuilder {

	final TypeUtils reflector = ContextProvider.getApplicationContext().getBean(TypeUtils.class);

	<T> T execute();

	ServiceBuilder persist(Object instance);

}
