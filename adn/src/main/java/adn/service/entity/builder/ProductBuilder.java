/**
 * 
 */
package adn.service.entity.builder;

import static adn.helpers.Base32.crockfords;
import static adn.helpers.StringHelper.normalizeString;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.hibernate.internal.util.collections.CollectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import adn.application.context.ContextProvider;
import adn.model.Generic;
import adn.model.entities.Product;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Product.class)
public class ProductBuilder extends AbstractPermanentEntityBuilder<Product> {

	private static final Logger logger = LoggerFactory.getLogger(ProductBuilder.class);
	private static final Character DELIMITER = '-';
	private static final String CODE_TEMPLATE = String.format("%s%s%s", "%s", DELIMITER, "%s");

	@Override
	protected <E extends Product> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setMaterial(normalizeString(model.getMaterial()));
		target.setDescription(normalizeString(model.getDescription()));

		List<String> images = model.getImages();

		target.setImages(CollectionHelper.isEmpty(images) ? null : images);

		return target;
	}

	@Override
	public <E extends Product> E buildInsertion(Serializable id, E model) {
		model = super.buildInsertion(id, model);

		model.setLocked(Optional.ofNullable(model.isLocked()).orElse(Boolean.FALSE));

		if (model.getCategory() != null) {
			if (logger.isDebugEnabled()) {
				ContextProvider.getCurrentSession().persist(model);

				id = model.getId();
				logger.debug(String.format("Generating code for product with id: [%s]", id));

				model.setCode(String.format(CODE_TEMPLATE, model.getCategory().getCode(),
						crockfords.format(new BigInteger(id.toString()))));
			}
		}

		return model;
	}

}
