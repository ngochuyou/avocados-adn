package adn.service.entity.builder;

import java.time.LocalDate;

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
	public Admin insertionBuild(Admin entity) {
		super.insertionBuild(entity);

		entity.setContractDate(LocalDate.now());

		return entity;
	}

}
