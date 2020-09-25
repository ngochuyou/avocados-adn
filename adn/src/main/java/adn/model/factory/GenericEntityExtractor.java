package adn.model.factory;

import adn.application.context.ContextProvider;
import adn.model.entities.Entity;
import adn.model.models.Model;
import adn.utilities.ClassReflector;

public interface GenericEntityExtractor<T extends Entity, M extends Model> extends EntityExtractor<T, M> {

	final ClassReflector reflector = ContextProvider.getApplicationContext().getBean(ClassReflector.class);

	<E extends T> E map(T model, E target);

}
