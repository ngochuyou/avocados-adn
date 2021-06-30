/**
 * 
 */
package adn.service.entity.builder;

import java.time.LocalDateTime;
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

	protected <E extends Factor> E mandatoryBuild(E target, E model) {
		target.setName(StringHelper.normalizeString(model.getName()));
		target.setActive(Optional.ofNullable(model.isActive()).orElse(true));

		return target;
	}

	@Override
	public T insertionBuild(T model) {
		super.insertionBuild(model);
		mandatoryBuild(model, model);

		model.setActive(true);
		model.setCreatedBy(ContextProvider.getPrincipalName());
		model.setUpdatedBy(model.getCreatedBy());

		return model;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T updateBuild(T model) {
		super.updateBuild(model);
		T persistence = (T) loadPersistence(model.getClass(), model.getId());

		mandatoryBuild(persistence, model);
		persistence.setUpdatedBy(ContextProvider.getPrincipalName());

		return persistence;
	}

	@Override
	public T deactivationBuild(T entity) {
		entity.setDeactivatedDate(LocalDateTime.now());

		return entity;
	}

}
