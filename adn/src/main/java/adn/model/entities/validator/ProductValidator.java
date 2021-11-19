/**
 * 
 */
package adn.model.entities.validator;

import static adn.application.Common.hasLength;
import static adn.application.Common.symbolNamesOf;

import java.io.Serializable;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.application.Common;
import adn.application.Result;
import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Product;
import adn.model.entities.metadata._Product;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Product.class)
public class ProductValidator extends AbstractPermanentEntityValidator<Product> {
	// @formatter:off
	private static final String INVALID_MATERIAL = String.format(
			"Material information can only contain alphabetic, numeric, %s characters and %s",
			symbolNamesOf('\s', '/', '\'', '"', '-', '_'),
			hasLength(null, null, _Product.MAXIMUM_MATERIAL_LENGTH));
	private static final String TOO_MANY_IMAGES = "Unable to upload more than 20 images";
	private static final String INVALID_DESCRIPTION = String.format(
			"Description can only contain alphabetic, numeric, %s characters and %s",
			symbolNamesOf(
					'.', ',', '[', ']', '_', '-', '+', '=',
					'/', '\\', '!', '@', '#', '$', '%', '^',
					'&', '*', '\'', '"', '?', '\s'),
			hasLength(null, null, Common.MYSQL_TEXT_MAX_LENGTH));
	private static final String MISSING_CATEGORY = Common.notEmpty("Category information");
	// @formatter:on
	@Override
	public Result<Product> isSatisfiedBy(Session session, Serializable id, Product instance) {
		Result<Product> result = super.isSatisfiedBy(session, id, instance);

		if (StringHelper.hasLength(instance.getMaterial())
				&& !_Product.MATERIAL_PATTERN.matcher(instance.getMaterial()).matches()) {
			result.bad(_Product.material, INVALID_MATERIAL);
		}

		if (instance.getImages() != null && instance.getImages().size() > _Product.MAXIMUM_IMAGES_AMOUNT) {
			result.bad(_Product.images, TOO_MANY_IMAGES);
		}

		if (StringHelper.hasLength(instance.getDescription())
				&& !_Product.DESCRIPTION_PATTERN.matcher(instance.getDescription()).matches()) {
			result.bad(_Product.description, INVALID_DESCRIPTION);
		}

		if (instance.getCategory() == null) {
			result.bad(_Product.category, MISSING_CATEGORY);
		}

		return result;
	}

}
