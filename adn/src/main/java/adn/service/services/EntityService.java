/**
 * 
 */
package adn.service.services;

import java.util.Date;

import org.springframework.stereotype.Service;

import adn.model.Entity;
import adn.service.ApplicationService;
import adn.service.GenericService;

/**
 * @author Ngoc Huy
 *
 */
@Service
@GenericService(target = Entity.class)
public class EntityService implements ApplicationService<Entity> {

	@Override
	public Entity doDeactivationProcedure(Entity model) {
		// TODO Auto-generated method stub
		model.setDeactivatedDate(new Date());

		return model;
	}

}
