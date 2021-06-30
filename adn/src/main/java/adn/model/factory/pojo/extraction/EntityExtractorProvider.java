package adn.model.factory.pojo.extraction;

import adn.application.context.ContextBuilder;
import adn.model.AbstractModel;

public interface EntityExtractorProvider extends ContextBuilder {

	<T extends AbstractModel, M extends AbstractModel> PojoEntityExtractor<T, M> getExtractor(Class<T> entityClass);

}
