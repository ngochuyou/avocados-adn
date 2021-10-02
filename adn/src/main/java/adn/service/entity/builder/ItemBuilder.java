/**
 * 
 */
package adn.service.entity.builder;

import static adn.helpers.StringHelper.normalizeString;

import java.io.Serializable;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Item;

/**
 * @author Ngoc Huy
 *
 */
@Generic(entityGene = Item.class)
@Component
public class ItemBuilder extends AbstractPermanentEntityBuilder<Item> {

	@Override
	protected <E extends Item> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setNamedSize(model.getNamedSize());
		target.setNumericSize(model.getNumericSize());
		target.setColor(normalizeString(model.getColor()));
		target.setNote(normalizeString(model.getNote()));
		target.setStatus(model.getStatus());

		return target;
	}

	@Override
	public <E extends Item> E buildInsertion(Serializable id, E model) {
		model = super.buildInsertion(id, model);

		if (model.getCost() != null) {
			model.setCost(model.getCost().setScale(4, RoundingMode.HALF_UP));
		}

		return model;
	}

}
