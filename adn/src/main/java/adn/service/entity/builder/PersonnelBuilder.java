/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import adn.application.context.ContextProvider;
import adn.model.Generic;
import adn.model.entities.Personnel;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Personnel.class)
public class PersonnelBuilder extends AccountBuilder<Personnel> {

	@Override
	public <E extends Personnel> E buildInsertion(Serializable id, E entity) {
		entity = super.buildInsertion(id, entity);

		entity.setCreatedBy(ContextProvider.getPrincipalName());

		return entity;
	}

}
