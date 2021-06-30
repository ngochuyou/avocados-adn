/**
 * 
 */
package adn.model.factory.pojo.extraction;

import adn.model.AbstractModel;
import adn.model.factory.EntityExtractor;

/**
 * @author Ngoc Huy
 *
 */
public interface PojoEntityExtractor<T extends AbstractModel, M extends AbstractModel> extends EntityExtractor<T, M> {

	@Override
	T extract(M model);

	@Override
	T extract(M source, T target);

}
