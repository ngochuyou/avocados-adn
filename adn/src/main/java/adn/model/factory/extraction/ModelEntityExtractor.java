package adn.model.factory.extraction;

import adn.model.Genetized;
import adn.model.entities.Entity;
import adn.model.factory.EntityExtractor;
import adn.model.models.Model;

@Genetized(entityGene = Entity.class)
public class ModelEntityExtractor<T extends Entity, M extends Model> implements EntityExtractor<T, M> {

	@Override
	public T extract(M model, T entity) {
		// TODO Auto-generated method stub
		entity.setActive(model.isActive());
		entity.setCreatedDate(model.getCreatedDate());
		entity.setUpdatedDate(model.getUpdatedDate());
		entity.setDeactivatedDate(model.getDeactivatedDate());

		return entity;
	}

}
