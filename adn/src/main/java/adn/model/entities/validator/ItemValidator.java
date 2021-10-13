/**
 * 
 */
package adn.model.entities.validator;

import static adn.application.Common.hasLength;
import static adn.application.Common.notEmpty;
import static adn.application.Common.symbolNamesOf;

import java.io.Serializable;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.application.Result;
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

	private static final String MISSING_SIZE = "Both named size and numeric size are missing, provide at least one of them";
	private static final String MAXIMUM_NUMERIC_SIZE_EXCEEDED = hasLength("Numeric size",
			_Item.MINIMUM_NUMERIC_SIZE_VALUE, _Item.MAXIMUM_NUMERIC_SIZE_VALUE);
	private static final String INVALID_COLOR = String.format(
			"Color pattern must either be: Named color in %d maximum characters or HEX color",
			_Item.MAXIMUM_NAMED_COLOR_LENGTH);
	private static final String MISSING_STATUS = notEmpty("Status");
	// @formatter:off
	private static final String INVALID_NOTE = String
			.format("Description can only contain alphabetic, numeric characters, %s and %s",
					symbolNamesOf('\s', ',', '.', '_', '-', '\"', '\'',
							'!', '@', '#', '$', '%', '&', '*'),
					hasLength(null, null, _Item.MAXIMUM_NOTE_LENGTH));
	// @formatter:on
	private static final String MISSING_PROVIDER = notEmpty("Provider information");
	private static final String MISSING_PRODUCT = notEmpty("Product information");

	@Override
	public Result<Item> isSatisfiedBy(Session session, Serializable id, Item instance) {
		Result<Item> result = super.isSatisfiedBy(session, id, instance);

		if (instance.getNamedSize() == null && instance.getNumericSize() == null) {
			result.bad(_Item.numericSize, MISSING_SIZE);
			result.getMessages().put(_Item.namedSize, result.getMessages().get(_Item.numericSize));
		}

		if (instance.getNumericSize() != null && (instance.getNumericSize() < _Item.MINIMUM_NUMERIC_SIZE_VALUE
				|| instance.getNumericSize() > _Item.MAXIMUM_NUMERIC_SIZE_VALUE)) {
			result.bad(_Item.numericSize, MAXIMUM_NUMERIC_SIZE_EXCEEDED);
		}

		if (instance.getColor() == null || !_Item.COLOR_PATTERN.matcher(instance.getColor()).matches()) {
			result.bad(_Item.color, INVALID_COLOR);
		}

		if (instance.getStatus() == null) {
			result.bad(_Item.status, MISSING_STATUS);
		}

		if (instance.getNote() != null && !_Item.NOTE_PATTERN.matcher(instance.getNote()).matches()) {
			result.bad(_Item.note, INVALID_NOTE);
		}

		if (instance.getProvider() == null) {
			result.bad(_Item.provider, MISSING_PROVIDER);
		}

		if (instance.getProduct() == null) {
			result.bad(_Item.product, MISSING_PRODUCT);
		}

		return result;
	}

}
