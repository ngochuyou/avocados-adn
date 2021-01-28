package adn.service;

import adn.application.context.ContextProvider;
import adn.utilities.GeneralUtilities;

public interface ServiceBuilder {

	final GeneralUtilities reflector = ContextProvider.getApplicationContext().getBean(GeneralUtilities.class);

	<T> T execute();

	ServiceBuilder persist(Object instance);

}
