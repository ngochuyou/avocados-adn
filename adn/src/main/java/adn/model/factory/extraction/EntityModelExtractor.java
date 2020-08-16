package adn.model.factory.extraction;

import adn.model.Entity;
import adn.model.Model;
import adn.model.factory.EntityExtractor;

public class EntityModelExtractor<E extends Entity, M extends Model> implements EntityExtractor<E, M> {

	@Override
	public E extract(M model, E entity) {
		// TODO Auto-generated method stub
		entity.setActive(model.isActive());
		entity.setCreatedDate(model.getCreatedDate());
		entity.setUpdatedDate(model.getUpdatedDate());
		entity.setDeactivatedDate(model.getDeactivatedDate());

		return entity;
	}

}
