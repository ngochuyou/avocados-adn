/**
 * 
 */
package adn.model.factory.dictionary.production.authentication;

import java.util.Map;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Provider;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Provider.class)
public class ProviderModelProducer extends AbstractCompositeAuthenticationBasedModelProducerImplementor<Provider> {

	@Override
	protected Map<String, Object> produceForPersonnel(Provider entity, Map<String, Object> model) {
		model.put("id", entity.getId());
		model.put("email", entity.getEmail());
		model.put("address", entity.getAddress());
		model.put("phoneNumbers", entity.getPhoneNumbers());
		model.put("representatorName", entity.getRepresentatorName());
		model.put("website", entity.getWebsite());
		
		return model;
	}

}
