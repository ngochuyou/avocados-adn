/**
 * 
 */
package adn.service.entity.builder;

import static adn.helpers.Base32.crockfords;
import static adn.helpers.StringHelper.normalizeString;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.dao.generic.GenericRepository;
import adn.model.Generic;
import adn.model.entities.Category;
import adn.model.entities.Product;
import adn.model.entities.metadata._Category;

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
	private static final String CATEGORY_NOT_FOUND = "Category not found";

	private final GenericRepository genericRepository;

	@Autowired
	public ProductBuilder(GenericRepository genericRepository) {
		super();
		this.genericRepository = genericRepository;
	}

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
	public <E extends Product> E buildInsertion(Serializable id, E model, Session session) {
		model = super.buildInsertion(id, model, session);

		model.setLocked(Optional.ofNullable(model.isLocked()).orElse(Boolean.FALSE));

		return model;
	}

	@Override
	public <E extends Product> E buildUpdate(Serializable id, E model, E persistence, Session session) {
		persistence = super.buildUpdate(id, model, persistence, session);

		persistence.setCategory(model.getCategory());

		return generateCode(persistence);
	}

	@Override
	public <E extends Product> E buildPostValidationOnInsert(Serializable id, E model, Session session) {
		session.persist(model);

		return generateCode(model);
	}

	private <E extends Product> E generateCode(E model) {
		BigInteger id = model.getId();

		if (logger.isDebugEnabled()) {
			logger.debug(String.format(CODE_GENERATION_MESSAGE, id));
		}

		Category category = model.getCategory();
		Optional<Object[]> tuple = genericRepository.findById(Category.class, category.getId(),
				Arrays.asList(_Category.code));

		if (tuple.isEmpty()) {
			throw new IllegalArgumentException(CATEGORY_NOT_FOUND);
		}

		model.setCode(String.format(CODE_TEMPLATE, tuple.get()[0], crockfords.format(new BigInteger(id.toString()))));

		return model;
	}

}
