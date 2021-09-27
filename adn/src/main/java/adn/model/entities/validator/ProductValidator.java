/**
 * 
 */
package adn.model.entities.validator;

import static adn.application.Common.symbolNamesOf;

import java.io.Serializable;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.dao.generic.Result;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Product;
import adn.model.entities.metadata._Product;
import adn.service.services.ProductService;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Product.class)
public class ProductValidator extends AbstractFullyAuditedValidator<Product> {

	private static final Pattern MATERIAL_PATTERN = Pattern.compile(String.format("^[%s\\p{L}\\p{N}\s/]{0,%d}$",
			StringHelper.VIETNAMESE_CHARACTERS, _Product.MAXIMUM_MATERIAL_LENGTH));

	private static final String INVALID_MATERIAL = String.format(
			"Material information can only contain alphabetic, numeric characters, %s and must not exceed %d characters",
			symbolNamesOf('\s', '/'), _Product.MAXIMUM_MATERIAL_LENGTH);

	@Override
	public Result<Product> isSatisfiedBy(Session session, Product instance) {
		return isSatisfiedBy(session, HibernateHelper.getIdentifier(instance), instance);
	}

	@Override
	public Result<Product> isSatisfiedBy(Session session, Serializable id, Product instance) {
		Result<Product> result = super.isSatisfiedBy(session, id, instance);

		if (!MATERIAL_PATTERN.matcher(instance.getMaterial()).matches()) {
			result.bad().getMessages().put(_Product.material, INVALID_MATERIAL);
		}

		if (instance.getImages() != null && instance.getImages().size() > ProductService.MAXIMUM_IMAGES_AMOUNT) {
			result.bad().getMessages().put("price", "Unable to upload more than 20 images");
		}

		if (instance.getCategory() == null) {
			result.bad().getMessages().put("category", "Missing category informations");
		}

		return result;
	}

}
