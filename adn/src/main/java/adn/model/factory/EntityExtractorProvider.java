package adn.model.factory;

import adn.application.context.ContextBuilder;
import adn.model.entities.Entity;
import adn.model.models.Model;

public interface EntityExtractorProvider extends ContextBuilder {

	<T extends Entity, M extends Model> EntityExtractor<T, M> getExtractor(Class<T> entityClass);

}
