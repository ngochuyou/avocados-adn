package adn.model.factory.dictionary.production.authentication;

import java.util.Map;

import org.springframework.stereotype.Component;

import adn.helpers.Utils;
import adn.model.Generic;
import adn.model.entities.Admin;

@Component
@Generic(entityGene = Admin.class)
public class AdminModelProducer extends AbstractCompositeAuthenticationBasedModelProducerImplementor<Admin> {

	@Override
	protected Map<String, Object> produceForAdmin(Admin admin, Map<String, Object> model) {
		model.put("contractDate", Utils.localDateToDate(admin.getContractDate()));

		return model;
	}

}
