/**
 * 
 */
package adn.dao.generic;

import java.util.Date;

import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import adn.dao.BaseDAO;
import adn.dao.GenericDAO;
import adn.model.Genetized;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Repository
@Genetized(entityGene = Entity.class)
public class EntityDAO<T extends Entity> extends BaseDAO implements GenericDAO<T> {

	private final String nullModel = "model parameter cannot be null";

	@Override
	public T defaultBuild(T model) {
		// TODO Auto-generated method stub
		Assert.notNull(model, nullModel);

		return model;
	}

	@Override
	public T insertionBuild(T model) {
		// TODO Auto-generated method stub
		Assert.notNull(model, nullModel);

		return model;
	}

	@Override
	public T updateBuild(T model) {
		// TODO Auto-generated method stub
		Assert.notNull(model, nullModel);

		return model;
	}

	@Override
	public T deactivationBuild(T model) {
		// TODO Auto-generated method stub
		Assert.notNull(model, nullModel);
		model.setDeactivatedDate(new Date());

		return model;
	}

}
