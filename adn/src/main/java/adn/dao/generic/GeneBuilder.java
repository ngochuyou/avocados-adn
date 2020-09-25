package adn.dao.generic;

import adn.model.AbstractModel;

public interface GeneBuilder<T extends AbstractModel> {

	T build(T instance);
	
}
