package adn.service.entity;

import adn.model.AbstractModel;

public interface GeneBuilder<T extends AbstractModel> {

	T build(T instance) throws RuntimeException;

	GeneBuilder<T> insertion();

	GeneBuilder<T> update();

	GeneBuilder<T> deactivation();

}
