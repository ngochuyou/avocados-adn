/**
 * 
 */
package adn.service.entity;

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
	public Personnel insertionBuild(final Personnel model) {
		Personnel persistence = super.insertionBuild(model);

		persistence.setCreatedBy(ContextProvider.getPrincipalName());

		return model;
	}

}
