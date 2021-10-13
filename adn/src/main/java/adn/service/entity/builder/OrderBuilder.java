/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Customer;
import adn.model.entities.Order;
import adn.model.entities.constants.OrderStatus;
import adn.service.services.AuthenticationService;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Order.class)
public class OrderBuilder extends AbstractPermanentEntityBuilder<Order> {

//	private static final Logger logger = LoggerFactory.getLogger(OrderBuilder.class);

	private final AuthenticationService authService;

	@Autowired
	public OrderBuilder(AuthenticationService authService) {
		super();
		this.authService = authService;
	}

	@Override
	protected <E extends Order> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setAddress(StringHelper.normalizeString(model.getAddress()));

		if (model.getDeliveryFee() != null) {
			target.setDeliveryFee(model.getDeliveryFee().setScale(4, RoundingMode.HALF_UP));
		}

		target.setItems(model.getItems().stream().filter(Objects::nonNull).collect(Collectors.toList()));

		return target;
	}

	@Override
	public <E extends Order> E buildInsertion(Serializable id, E model) {
		model = super.buildInsertion(id, model);

		model.setCreatedTimestamp(LocalDateTime.now());
		model.setStatus(OrderStatus.PENDING_PAYMENT);

		Customer customer = authService.getCustomer();

		model.setCustomer(customer);
		model.setUpdatedBy(customer);
		model.setHandledBy(null);

		return model;
	}

	@Override
	public <E extends Order> E buildUpdate(Serializable id, E model, E persistence) {
		persistence = super.buildUpdate(id, model, persistence);

		persistence.setUpdatedBy(authService.getCustomer());

		return persistence;
	}

//	@Override
//	public <E extends Order> E buildPostValidationOnInsert(Serializable id, E model) {
//		ContextProvider.getCurrentSession().persist(model);
//		id = model.getId();
//
//		if (logger.isDebugEnabled()) {
//			logger.debug(String.format(CODE_GENERATION_MESSAGE, id));
//		}
//
//		model.setCode(crockfords.format((BigInteger) id));
//
//		return model;
//	}

}
