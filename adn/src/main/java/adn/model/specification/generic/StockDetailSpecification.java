/**
 * 
 */
package adn.model.specification.generic;

import static adn.model.entities.StockDetail.DESCRIPTION_MAXIMUM_LENGTH;
import static adn.model.entities.StockDetail.MATERIAL_MAXIMUM_LENGTH;
import static adn.model.entities.StockDetail.NAMED_COLOR_MAXIMUM_LENGTH;
import static adn.model.entities.StockDetail.NAMED_SIZE_MAXIMUM_LENGTH;
import static adn.model.entities.StockDetail.NUMERIC_SIZE_MAXIMUM_VALUE;
import static adn.model.entities.StockDetail.NUMERIC_SIZE_MINIMUM_VALUE;
import static adn.model.entities.StockDetail.STATUS_MAXIMUM_LENGTH;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.dao.generic.Result;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.StockDetail;

/**
 * @author Ngoc Huy
 *
 */
@Generic(entityGene = StockDetail.class)
@Component
public class StockDetailSpecification extends EntitySpecification<StockDetail> {

	private static final Pattern COLOR_PATTERN = Pattern
			.compile(String.format("^(([A-Za-z\\s]{0,%d})|(#(?:[0-9a-fA-F]{3,4}){1,2}))$", NAMED_COLOR_MAXIMUM_LENGTH));
	private static final Pattern DESCRIPTION_PATTERN = Pattern
			.compile(String.format("^[%s\\p{L}\\p{N}\s\n\\(\\)\\._\\-\"\'\\!@#$%%^&*]{0,%d}$",
					StringHelper.VIETNAMESE_CHARACTERS, DESCRIPTION_MAXIMUM_LENGTH));

	@Override
	public Result<StockDetail> isSatisfiedBy(Session session, StockDetail instance) {
		return isSatisfiedBy(session, HibernateHelper.getIdentifier(instance), instance);
	}

	@Override
	public Result<StockDetail> isSatisfiedBy(Session session, Serializable id, StockDetail instance) {
		Result<StockDetail> result = super.isSatisfiedBy(session, id, instance);

		if (instance.getProduct() == null) {
			result.bad().getMessages().put("product", "Product information is missing");
		}

		if (instance.getStockedBy() == null) {
			result.bad().getMessages().put("stockedBy", "Missing stocked-by information");
		}

		if (instance.getSize() == null && instance.getNumericSize() == null) {
			result.bad().getMessages().put("numericSize",
					"Both named size and numeric size is missing, provide at least one of them");
			result.getMessages().put("namedSize", result.getMessages().get("numericSize"));
		}

		if (instance.getSize() != null && instance.getSize().name().length() > NAMED_SIZE_MAXIMUM_LENGTH) {
			result.bad().getMessages().put("namedSize",
					String.format("Size name can not be longer than", NAMED_SIZE_MAXIMUM_LENGTH));
		}

		if (instance.getNumericSize() != null && (instance.getNumericSize() < NUMERIC_SIZE_MINIMUM_VALUE
				|| instance.getNumericSize() > NUMERIC_SIZE_MAXIMUM_VALUE)) {
			result.bad().getMessages().put("numericSize", String.format("Numeric size must vary between %d and %d",
					NUMERIC_SIZE_MINIMUM_VALUE, NUMERIC_SIZE_MAXIMUM_VALUE));
		}

		if (!COLOR_PATTERN.matcher(instance.getColor()).matches()) {
			result.bad().getMessages().put("color",
					String.format("Color pattern must either be: Named color in %d maximum characters or HEX color",
							NAMED_COLOR_MAXIMUM_LENGTH));
		}

		if (instance.getMaterial() != null && instance.getMaterial().length() > MATERIAL_MAXIMUM_LENGTH) {
			result.bad().getMessages().put("material", String
					.format("Material information can not be longer than %d characters", MATERIAL_MAXIMUM_LENGTH));
		}

		if (instance.getStockedTimestamp() == null || instance.getStockedTimestamp().isAfter(LocalDateTime.now())) {
			result.bad().getMessages().put("stockedTimestamp",
					"Stocked date can not be empty and must not be in the future");
		}
		
		if (!StringHelper.hasLength(instance.getUpdatedBy())) {
			result.bad().getMessages().put("updatedBy", "Updated by information is missing");
		}

		if (instance.getStatus() == null || instance.getStatus().name().length() > STATUS_MAXIMUM_LENGTH) {
			result.bad().getMessages().put("status", String
					.format("Status can not be empty and can not be longer than %d characters", STATUS_MAXIMUM_LENGTH));
		}

		if (instance.isActive() == null) {
			result.bad().getMessages().put("active", "Active state can bot be empty");
		}

		if (instance.getDescription() != null && !DESCRIPTION_PATTERN.matcher(instance.getDescription()).matches()) {
			result.bad().getMessages().put("description", "Invalid description");
		}

		if (instance.getProvider() == null) {
			result.bad().getMessages().put("provider", "Provider information is missing");
		}

		return result;
	}

}
