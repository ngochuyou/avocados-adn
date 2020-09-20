package adn.model.factory;

import adn.application.context.ContextBuilder;
import adn.model.entities.Entity;
import adn.model.models.Model;

public interface ModelProducerProvider extends ContextBuilder {

	<T extends Entity, M extends Model> ModelProducer<T, M> getProducer(Class<M> modelClass);

}
