/**
 * 
 */
package adn.model.entities.validator;

import static adn.application.Common.notEmpty;
import static adn.application.Common.notNegative;
import static adn.application.Common.symbolNamesOf;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.dao.generic.Result;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Item;
import adn.model.entities.metadata._Item;

/**
 * @author Ngoc Huy
 *
 */
@Generic(entityGene = Item.class)
@Component
public class ItemValidator extends AbstractPermanentEntityValidator<Item> {

	private static final Pattern COLOR_PATTERN = Pattern.compile(String
			.format("^(([A-Za-z\\s]{0,%d})|(#(?:[0-9a-fA-F]{3,4}){1,2}))$", _Item.MAXIMUM_NAMED_COLOR_LENGTH));
	private static final Pattern NOTE_PATTERN = Pattern
			.compile(String.format("^[%s\\p{L}\\p{N}\s\n\\.\\_\\-\"\'!@#$%%&*,]{0,%d}$",
					StringHelper.VIETNAMESE_CHARACTERS, _Item.MAXIMUM_DESCRIPTION_LENGTH));

	private static final String MISSING_PRODUCT = notEmpty("Product information");
	private static final String MISSING_SIZE = "Both named size and numeric size are missing, provide at least one of them";
	private static final String MAXIMUM_NUMERIC_SIZE_EXCEEDED = String.format(
			"Numeric size must vary between %d and %d characters", _Item.MINIMUM_NUMERIC_SIZE_VALUE,
			_Item.MAXIMUM_NUMERIC_SIZE_VALUE);
	private static final String INVALID_COLOR = String.format(
			"Color pattern must either be: Named color in %d maximum characters or HEX color",
			_Item.MAXIMUM_NAMED_COLOR_LENGTH);
	private static final String MISSING_STATUS = notEmpty("Status");
	private static final String MISSING_COST = notEmpty("Cost information");
	private static final String NEGATIVE_COST = notNegative("Cost");
	// @formatter:off
	private static final String INVALID_NOTE = String
			.format("Description can only contain alphabetic, numeric characters, %s and must not exceed %d characters",
					symbolNamesOf('\s', ',', '.', '_', '-', '\"', '\'', '!', '@', '#', '$', '%', '&', '*'),
					_Item.MAXIMUM_DESCRIPTION_LENGTH);
	// @formatter:on
	private static final String MISSING_PROVIDER = notEmpty("Provider information");

	@Override
	public Result<Item> isSatisfiedBy(Session session, Item instance) {
		return isSatisfiedBy(session, HibernateHelper.getIdentifier(instance), instance);
	}

	@Override
	public Result<Item> isSatisfiedBy(Session session, Serializable id, Item instance) {
		Result<Item> result = super.isSatisfiedBy(session, id, instance);

		if (instance.getProduct() == null) {
			result.bad().getMessages().put(_Item.product, MISSING_PRODUCT);
		}

		if (instance.getNamedSize() == null && instance.getNumericSize() == null) {
			result.bad().getMessages().put(_Item.numericSize, MISSING_SIZE);
			result.getMessages().put(_Item.namedSize, result.getMessages().get(_Item.numericSize));
		}

		if (instance.getNumericSize() != null && (instance.getNumericSize() < _Item.MINIMUM_NUMERIC_SIZE_VALUE
				|| instance.getNumericSize() > _Item.MAXIMUM_NUMERIC_SIZE_VALUE)) {
			result.bad().getMessages().put(_Item.numericSize, MAXIMUM_NUMERIC_SIZE_EXCEEDED);
		}

		if (!COLOR_PATTERN.matcher(instance.getColor()).matches()) {
			result.bad().getMessages().put(_Item.color, INVALID_COLOR);
		}

		if (instance.getStatus() == null) {
			result.bad().getMessages().put(_Item.status, MISSING_STATUS);
		}

		if (instance.getNote() != null && !NOTE_PATTERN.matcher(instance.getNote()).matches()) {
			result.bad().getMessages().put(_Item.note, INVALID_NOTE);
		}

		if (instance.getProvider() == null) {
			result.bad().getMessages().put(_Item.provider, MISSING_PROVIDER);
		}

		if (instance.getCost() != null) {
			if (instance.getCost().compareTo(BigDecimal.ZERO) < 0) {
				result.bad().getMessages().put(_Item.cost, NEGATIVE_COST);
			}
		} else {
			result.bad().getMessages().put(_Item.cost, MISSING_COST);
		}

		return result;
	}

}
