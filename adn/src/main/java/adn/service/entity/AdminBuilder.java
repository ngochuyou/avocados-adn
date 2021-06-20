package adn.service.entity;

import java.util.Date;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Admin;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Admin.class)
public class AdminBuilder extends AccountBuilder<Admin> {

	@Override
	public Admin insertionBuild(final Admin model) {
		Admin persistence = super.insertionBuild(model);

		persistence.setContractDate(new Date());

		return persistence;
	}

}
