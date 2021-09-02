/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Factor;
import adn.service.services.AuthenticationService;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Factor.class)
public class FactorBuilder<T extends Factor> extends PermanentEntityBuilder<T> {

	private final AuthenticationService authService;

	@Autowired
	public FactorBuilder(AuthenticationService authService) {
		super();
		this.authService = authService;
	}

	protected <E extends T> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setName(StringHelper.normalizeString(model.getName()));

		return target;
	}

	@Override
	public <E extends T> E buildInsertion(Serializable id, E model) {
		model = super.buildInsertion(id, model);

		model.setCreatedBy(authService.getOperator());
		model.setUpdatedBy(model.getCreatedBy());
		model.setDeactivatedTimestamp(null);
		model.setApprovedBy(null);
		model.setApprovedTimestamp(null);

		return model;
	}

	@Override
	public <E extends T> E buildUpdate(Serializable id, E model, E persistence) {
		persistence = super.buildUpdate(id, model, persistence);

		persistence.setUpdatedBy(authService.getOperator());

		return persistence;
	}

}
