/**
 * 
 */
package adn.service.entity.builder;

import static adn.helpers.Base32.crockfords;

import java.io.Serializable;
import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import adn.application.context.ContextProvider;
import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Category;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Category.class)
public class CategoryBuilder extends AbstractPermanentEntityBuilder<Category> {

	private static final Logger logger = LoggerFactory.getLogger(CategoryBuilder.class);

	@Override
	protected <E extends Category> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setDescription(StringHelper.normalizeString(model.getDescription()));

		return target;
	}

	@Override
	public <E extends Category> E buildPostValidationOnInsert(Serializable id, E model) {
		ContextProvider.getCurrentSession().persist(model);
		id = model.getId();

		if (logger.isDebugEnabled()) {
			logger.debug(String.format(CODE_GENERATION_MESSAGE, id));
		}

		model.setCode(crockfords.format(new BigInteger(id.toString())));

		return model;
	}

}
