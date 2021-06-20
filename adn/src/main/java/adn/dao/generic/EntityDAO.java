/**
 * 
 */
package adn.dao.generic;

import java.util.Date;

import org.springframework.stereotype.Repository;

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

	protected final String nullModel = "model parameter cannot be null";

	@Override
	public T deactivationBuild(T model) {
		model.setDeactivatedDate(new Date());

		return model;
	}

}
