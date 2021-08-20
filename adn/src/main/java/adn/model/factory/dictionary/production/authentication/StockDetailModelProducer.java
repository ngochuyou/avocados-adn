/**
 * 
 */
package adn.model.factory.dictionary.production.authentication;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.helpers.Utils;
import adn.model.Generic;
import adn.model.entities.Personnel;
import adn.model.entities.Product;
import adn.model.entities.Provider;
import adn.model.entities.StockDetail;
import adn.model.factory.AuthenticationBasedModelFactory;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = StockDetail.class)
public class StockDetailModelProducer
		extends AbstractCompositeAuthenticationBasedModelProducerImplementor<StockDetail> {

	@Autowired
	private AuthenticationBasedModelFactory factory;

	private Map<String, Object> produceForAll(StockDetail entity, Map<String, Object> model) {
		model.put("id", entity.getId());
		model.put("size", entity.getSize());
		model.put("numericSize", entity.getNumericSize());
		model.put("color", entity.getColor());
		model.put("material", entity.getMaterial());
		model.put("status", entity.getStatus());
		model.put("active", entity.isActive());
		model.put("descriptoin", entity.getDescription());

		return model;
	}

	@Override
	protected Map<String, Object> produceForAnonymous(StockDetail entity, Map<String, Object> model) {
		model = produceForAll(entity, model);

		model.put("product", factory.produce(Product.class, entity.getProduct()));

		return model;
	}

	@Override
	protected Map<String, Object> produceForAdmin(StockDetail entity, Map<String, Object> model) {
		return produceForAnonymous(entity, model);
	}

	@Override
	protected Map<String, Object> produceForPersonnel(StockDetail entity, Map<String, Object> model) {
		model = produceForAnonymous(entity, model);

		model.put("stockedDate", Utils.ldt(entity.getStockedTimestamp()));
		model.put("stockedBy", factory.produce(Personnel.class, entity.getStockedBy()));
		model.put("provider", factory.produce(Provider.class, entity.getProvider()));

		return model;
	}

}
