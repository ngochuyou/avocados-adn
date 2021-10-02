/**
 * 
 */
package adn.model.entities.validator;

import static adn.application.Common.hasLength;
import static adn.application.Common.notEmpty;
import static adn.application.Common.notNegative;
import static adn.application.Common.symbolNamesOf;

import java.io.Serializable;
import java.math.BigDecimal;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.dao.generic.Result;
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

	private static final String MISSING_PRODUCT = notEmpty("Product information");
	private static final String MISSING_SIZE = "Both named size and numeric size are missing, provide at least one of them";
	private static final String MAXIMUM_NUMERIC_SIZE_EXCEEDED = hasLength("Numeric size",
			_Item.MINIMUM_NUMERIC_SIZE_VALUE, _Item.MAXIMUM_NUMERIC_SIZE_VALUE);
	private static final String INVALID_COLOR = String.format(
			"Color pattern must either be: Named color in %d maximum characters or HEX color",
			_Item.MAXIMUM_NAMED_COLOR_LENGTH);
	private static final String MISSING_STATUS = notEmpty("Status");
	// @formatter:off
	private static final String INVALID_COST = String.format(
			"%s and %s",
			notEmpty("Cost information"), notNegative());
	private static final String INVALID_NOTE = String
			.format("Description can only contain alphabetic, numeric characters, %s and %s",
					symbolNamesOf('\s', ',', '.', '_', '-', '\"', '\'',
							'!', '@', '#', '$', '%', '&', '*'),
					hasLength(null, null, _Item.MAXIMUM_NOTE_LENGTH));
	// @formatter:on
	private static final String MISSING_PROVIDER = notEmpty("Provider information");

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

		if (!_Item.COLOR_PATTERN.matcher(instance.getColor()).matches()) {
			result.bad().getMessages().put(_Item.color, INVALID_COLOR);
		}

		if (instance.getStatus() == null) {
			result.bad().getMessages().put(_Item.status, MISSING_STATUS);
		}

		if (instance.getNote() != null && !_Item.NOTE_PATTERN.matcher(instance.getNote()).matches()) {
			result.bad().getMessages().put(_Item.note, INVALID_NOTE);
		}

		if (instance.getProvider() == null) {
			result.bad().getMessages().put(_Item.provider, MISSING_PROVIDER);
		}

		if (instance.getCost() == null || instance.getCost().compareTo(BigDecimal.ZERO) < 0) {
			result.bad().getMessages().put(_Item.cost, INVALID_COST);
		}

		return result;
	}

}
