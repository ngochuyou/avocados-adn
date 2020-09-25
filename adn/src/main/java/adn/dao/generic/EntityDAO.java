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
public class EntityDAO extends BaseDAO implements GenericDAO<Entity> {

	@Override
	public Entity deactivationBuild(Entity model) {
		// TODO Auto-generated method stub
		model.setDeactivatedDate(new Date());

		return model;
	}
	
}
