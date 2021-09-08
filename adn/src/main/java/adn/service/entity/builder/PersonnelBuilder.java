/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Personnel;
import adn.service.services.AuthenticationService;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Personnel.class)
public class PersonnelBuilder extends AccountBuilder<Personnel> {

	private final AuthenticationService authService;

	@Autowired
	public PersonnelBuilder(AuthenticationService authService) {
		super();
		this.authService = authService;
	}

	@Override
	public <E extends Personnel> E buildInsertion(Serializable id, E entity) {
		entity = super.buildInsertion(id, entity);

		entity.setCreatedBy(authService.getOperator());

		return entity;
	}

}
