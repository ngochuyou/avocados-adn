package adn.model.factory.extraction;

import adn.model.Genetized;
import adn.model.entities.Entity;
import adn.model.factory.EntityExtractor;
import adn.model.models.Model;

@Genetized(entityGene = Entity.class)
public class ModelEntityExtractor<E extends Entity, M extends Model> implements EntityExtractor<E, M> {

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
