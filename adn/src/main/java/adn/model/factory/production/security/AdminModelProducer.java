package adn.model.factory.production.security;

import adn.model.entities.Admin;
import adn.model.models.AdminModel;

public class AdminModelProducer<AM extends AdminModel, A extends Admin>
		implements ModelProducerBasedOnAuthentication<AM, A> {

	@Override
	public AM produceForAdminAuthentication(A entity, AM model) {
		// TODO Auto-generated method stub
		model.setContractDate(entity.getContractDate());

		return model;
	}

}
