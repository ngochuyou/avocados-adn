/**
 * 
 */
package adn.service.generic;

import java.util.Date;

import org.springframework.stereotype.Service;

import adn.model.Genetized;
import adn.model.entities.Entity;
import adn.service.GenericService;

/**
 * @author Ngoc Huy
 *
 */
@Service
@Genetized(entityGene = Entity.class)
public class EntityService implements GenericService<Entity> {

	@Override
	public Entity executeDeactivationProcedure(Entity model) {
		// TODO Auto-generated method stub
		model.setDeactivatedDate(new Date());

		return model;
	}

}
