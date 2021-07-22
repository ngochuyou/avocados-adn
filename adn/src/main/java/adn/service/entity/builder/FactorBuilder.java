/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;
import java.util.Optional;

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
		target.setName(StringHelper.normalizeString(model.getName()));
		target.setActive(Optional.ofNullable(model.isActive()).orElse(true));

		return target;
	}

	@Override
	public <E extends T> E insertionBuild(Serializable id, E model) {
		mandatoryBuild(model, model);

		model.setActive(Boolean.TRUE);
		model.setCreatedBy(ContextProvider.getPrincipalName());
		model.setUpdatedBy(model.getCreatedBy());
		model.setDeactivatedDate(null);

		return model;
	}

	@Override
	public <E extends T> E updateBuild(Serializable id, E model, E persistence) {
		persistence = mandatoryBuild(persistence, model);

		persistence.setUpdatedBy(ContextProvider.getPrincipalName());

		return persistence;
	}

}
