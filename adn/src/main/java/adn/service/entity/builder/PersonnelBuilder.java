/**
 * 
 */
package adn.service.entity.builder;

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
	public Personnel insertionBuild(Personnel entity) {
		super.insertionBuild(entity);

		entity.setCreatedBy(ContextProvider.getPrincipalName());

		return entity;
	}

}
