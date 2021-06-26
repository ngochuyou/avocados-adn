package adn.model.factory;

import adn.application.context.ContextBuilder;
import adn.model.AbstractModel;
import adn.model.entities.Entity;

public interface ModelProducerProvider extends ContextBuilder {

	<T extends Entity, M extends AbstractModel> ModelProducer<T, M> getProducer(Class<M> modelClass);

}
