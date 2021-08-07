package adn.model.factory.dictionary.production.authentication;

import java.util.Map;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Personnel;

@Component
@Generic(entityGene = Personnel.class)
public class PersonnelModelProducer extends AbstractCompositeAuthenticationBasedModelProducerImplementor<Personnel> {

	@Override
	protected Map<String, Object> produceForPersonnel(Personnel entity, Map<String, Object> model) {
		model.put("createdBy", entity.getCreatedBy());

		return model;
	}

}
