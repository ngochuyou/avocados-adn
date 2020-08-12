/**
 * 
 */
package adn.service.generic;

import java.util.Date;

import org.springframework.stereotype.Service;

import adn.model.Entity;
import adn.model.Genetized;
import adn.service.GenericService;

/**
 * @author Ngoc Huy
 *
 */
@Service
@Genetized(gene = Entity.class)
public class EntityService implements GenericService<Entity> {

	@Override
	public Entity doDeactivationProcedure(Entity model) {
		// TODO Auto-generated method stub
		model.setDeactivatedDate(new Date());

		return model;
	}

}
