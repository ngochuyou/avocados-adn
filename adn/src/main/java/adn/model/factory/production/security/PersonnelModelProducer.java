package adn.model.factory.production.security;

import java.util.Map;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Personnel;

@Component
@Generic(entityGene = Personnel.class)
public class PersonnelModelProducer extends AbstractCompositeAuthenticationBasedModelProducerImplementor<Personnel> {

	@Override
	protected Map<String, Object> produceForEmployee(Personnel account, Map<String, Object> model) {
		model.put("createdBy", account.getCreatedBy());

		return model;
	}

	@Override
	protected Map<String, Object> produceForManager(Personnel entity, Map<String, Object> model) {
		return produceForEmployee(entity, model);
	}

	@Override
	protected Map<String, Object> produceForPersonnel(Personnel entity, Map<String, Object> model) {
		return produceForEmployee(entity, model);
	}

	@Override
	protected Map<String, Object> produceForAdmin(Personnel entity, Map<String, Object> model) {
		return produceForEmployee(entity, model);
	}

}
