package adn.service.builder;

import adn.application.context.ContextProvider;
import adn.utilities.ClassReflector;

public interface ServiceBuilder {
	
	final ClassReflector reflector = ContextProvider.getApplicationContext().getBean(ClassReflector.class);
	
	<T> T execute();
	
	ServiceBuilder persist(Object instance);
	
	ServiceTransaction transaction();
	
}
