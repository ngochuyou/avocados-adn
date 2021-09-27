/**
 * 
 */
package adn.service.entity.builder;

import static adn.helpers.StringHelper.normalizeString;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Item;
import adn.service.services.AuthenticationService;

/**
 * @author Ngoc Huy
 *
 */
@Generic(entityGene = Item.class)
@Component
public class StockDetailBuilder extends AbstractEntityBuilder<Item> {

	private final AuthenticationService authService;

	@Autowired
	public StockDetailBuilder(AuthenticationService authService) {
		super();
		this.authService = authService;
	}

	@Override
	protected <E extends Item> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setColor(normalizeString(model.getColor()));
		target.setProvider(model.getProvider());
		target.setNote(normalizeString(model.getNote()));
		target.setProduct(model.getProduct());

		return target;
	}

	@Override
	public <E extends Item> E buildInsertion(Serializable id, E model) {
		model = super.buildInsertion(id, model);

		model.setCreatedBy(authService.getOperator());
		model.setLastModifiedBy(authService.getOperator());

		return model;
	}

	@Override
	public <E extends Item> E buildUpdate(Serializable id, E model, E persistence) {
		model = super.buildInsertion(id, model);

		model.setLastModifiedBy(authService.getOperator());

		return super.buildUpdate(id, model, persistence);
	}

}
