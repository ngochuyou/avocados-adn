/**
 * 
 */
package adn.service.entity.builder;

import static adn.helpers.Base32.crockfords;
import static adn.helpers.StringHelper.normalizeString;

import java.io.Serializable;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import adn.application.context.ContextProvider;
import adn.model.Generic;
import adn.model.entities.Item;
import adn.model.entities.constants.ItemStatus;

/**
 * @author Ngoc Huy
 *
 */
@Generic(entityGene = Item.class)
@Component
public class ItemBuilder extends AbstractPermanentEntityBuilder<Item> {

	private static final Logger logger = LoggerFactory.getLogger(ItemBuilder.class);

	@Override
	protected <E extends Item> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setNamedSize(model.getNamedSize());
		target.setNumericSize(model.getNumericSize());
		target.setColor(normalizeString(model.getColor()));
		target.setNote(normalizeString(model.getNote()));
		target.setStatus(Optional.ofNullable(model.getStatus()).orElse(ItemStatus.AVAILABLE));

		return target;
	}

	@Override
	public <E extends Item> E buildInsertion(Serializable id, E model) {
		model = super.buildInsertion(id, model);

		if (model.getCost() != null) {
			model.setCost(model.getCost().setScale(4, RoundingMode.HALF_UP));
		}

		if (model.getPrice() != null) {
			model.setPrice(model.getPrice().setScale(4, RoundingMode.HALF_UP));
		}

		return model;
	}

	@Override
	public <E extends Item> E buildPostValidationOnInsert(Serializable id, E model) {
		ContextProvider.getCurrentSession().persist(model);
		id = model.getId();

		if (logger.isDebugEnabled()) {
			logger.debug(String.format(CODE_GENERATION_MESSAGE, id));
		}

		model.setCode(crockfords.format((BigInteger) id));

		return model;
	}

}
