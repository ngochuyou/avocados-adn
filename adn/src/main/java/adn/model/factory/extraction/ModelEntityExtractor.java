package adn.model.factory.extraction;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import adn.model.Genetized;
import adn.model.entities.Entity;
import adn.model.factory.EntityExtractor;
import adn.model.models.Model;

@Component("modelEntityExtractor")
@Genetized(entityGene = Entity.class)
public class ModelEntityExtractor<T extends Entity, M extends Model> implements EntityExtractor<T, M> {

	@Override
	public T extract(M model, T entity) throws NullPointerException {
		// TODO Auto-generated method stub
		Assert.notNull(model, "Cannot perform extraction on null model");
		Assert.notNull(entity, "Cannot perform extraction on null entity");
		
		entity.setActive(model.isActive());
		entity.setCreatedDate(model.getCreatedDate());
		entity.setUpdatedDate(model.getUpdatedDate());
		entity.setDeactivatedDate(model.getDeactivatedDate());

		return entity;
	}

	@Override
	public <E extends T> E merge(T model, E target) throws NullPointerException {
		// TODO Auto-generated method stub
		if (model == null || target == null) {
			throw new NullPointerException("model and entity can not be null");
		}

		target.setActive(model.isActive());
		target.setCreatedDate(model.getCreatedDate());
		target.setUpdatedDate(model.getUpdatedDate());
		target.setDeactivatedDate(model.getDeactivatedDate());

		return target;
	}

}
