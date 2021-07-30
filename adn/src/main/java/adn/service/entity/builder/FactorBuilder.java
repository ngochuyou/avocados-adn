/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import adn.application.context.ContextProvider;
import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Factor;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Factor.class)
public class FactorBuilder<T extends Factor> extends AbstractEntityBuilder<T> {

	protected <E extends T> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setName(StringHelper.normalizeString(model.getName()));

		return target;
	}

	@Override
	public <E extends T> E insertionBuild(Serializable id, E model) {
		model = super.insertionBuild(id, model);

		model.setCreatedBy(ContextProvider.getPrincipalName());
		model.setUpdatedBy(model.getCreatedBy());
		model.setDeactivatedDate(null);

		return model;
	}

	@Override
	public <E extends T> E updateBuild(Serializable id, E model, E persistence) {
		persistence = super.updateBuild(id, model, persistence);

		persistence.setUpdatedBy(ContextProvider.getPrincipalName());

		return persistence;
	}

}
