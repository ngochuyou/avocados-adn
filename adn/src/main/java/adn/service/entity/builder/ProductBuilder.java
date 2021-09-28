/**
 * 
 */
package adn.service.entity.builder;

import static adn.helpers.StringHelper.normalizeString;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.internal.util.collections.CollectionHelper;
import org.springframework.stereotype.Component;

import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Product;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Product.class)
public class ProductBuilder extends AbstractPermanentEntityBuilder<Product> {

	@Override
	protected <E extends Product> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setMaterial(normalizeString(model.getMaterial()));
		target.setDescription(normalizeString(model.getDescription()));

		List<String> images = model.getImages();

		target.setImages(CollectionHelper.isEmpty(images) ? null
				: images.stream().filter(StringHelper::hasLength).collect(Collectors.toList()));

		return target;
	}

	@Override
	public <E extends Product> E buildInsertion(Serializable id, E model) {
		model = super.buildInsertion(id, model);

		model.setLocked(Optional.ofNullable(model.isLocked()).orElse(Boolean.FALSE));

		return model;
	}

}
