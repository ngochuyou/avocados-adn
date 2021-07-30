/**
 * 
 */
package adn.service.entity.builder;

import static adn.helpers.StringHelper.normalizeString;

import java.io.Serializable;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

import adn.application.context.ContextProvider;
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
	public <E extends StockDetail> E insertionBuild(Serializable id, E model) {
		model = super.insertionBuild(id, model);

		model.setStockedBy(new Personnel(ContextProvider.getPrincipalName()));
		model.setStockedDate(LocalDate.now());
		model.setSoldBy(null);

		return model;
	}

}
