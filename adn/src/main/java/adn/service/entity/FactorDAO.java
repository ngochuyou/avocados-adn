/**
 * 
 */
package adn.service.entity;

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
public class FactorDAO<T extends Factor> extends AbstractEntityBuilder<T> {

	@Override
	public T defaultBuild(T model) {
		T persistence = super.defaultBuild(model);

		persistence.setName(StringHelper.normalizeString(model.getName()));
		persistence.setCreatedBy(StringHelper.removeSpaces(model.getCreatedBy()));
		persistence.setUpdatedBy(StringHelper.removeSpaces(model.getUpdatedBy()));

		return persistence;
	}

	@Override
	public T insertionBuild(T model) {
		T persistence = super.insertionBuild(model);

		persistence.setCreatedBy(ContextProvider.getPrincipalName());

		return persistence;
	}

	@Override
	public T updateBuild(T model) {
		T persistence = super.updateBuild(model);

		persistence.setUpdatedBy(ContextProvider.getPrincipalName());

		return persistence;
	}

}
