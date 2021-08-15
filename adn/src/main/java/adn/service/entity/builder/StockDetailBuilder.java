/**
 * 
 */
package adn.service.entity.builder;

import static adn.application.context.ContextProvider.getPrincipalName;
import static adn.helpers.StringHelper.normalizeString;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Personnel;
import adn.model.entities.StockDetail;

/**
 * @author Ngoc Huy
 *
 */
@Generic(entityGene = StockDetail.class)
@Component
public class StockDetailBuilder extends AbstractEntityBuilder<StockDetail> {

	@Override
	protected <E extends StockDetail> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setColor(normalizeString(model.getColor()));
		target.setMaterial(normalizeString(model.getMaterial()));
		target.setProvider(model.getProvider());
		target.setDescription(normalizeString(model.getDescription()));
		target.setProduct(model.getProduct());

		return target;
	}

	@Override
	public <E extends StockDetail> E buildInsertion(Serializable id, E model) {
		model = super.buildInsertion(id, model);

		model.setStockedBy(new Personnel(getPrincipalName()));
		model.setUpdatedBy(getPrincipalName());
		
		return model;
	}

	@Override
	public <E extends StockDetail> E buildUpdate(Serializable id, E model, E persistence) {
		model = super.buildInsertion(id, model);

		model.setUpdatedBy(getPrincipalName());

		return super.buildUpdate(id, model, persistence);
	}

}
