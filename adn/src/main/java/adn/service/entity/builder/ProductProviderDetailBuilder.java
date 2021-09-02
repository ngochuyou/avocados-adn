/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.ProductProviderDetail;
import adn.service.services.AuthenticationService;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = ProductProviderDetail.class)
public class ProductProviderDetailBuilder extends PermanentEntityBuilder<ProductProviderDetail> {

	private final AuthenticationService authService;

	@Autowired
	public ProductProviderDetailBuilder(AuthenticationService authService) {
		super();
		this.authService = authService;
	}

	@Override
	protected <E extends ProductProviderDetail> E mandatoryBuild(E target, E model) {
		target.setPrice(model.getPrice().setScale(4, RoundingMode.HALF_UP));

		return target;
	}

	@Override
	public <E extends ProductProviderDetail> E buildInsertion(Serializable id, E model) {
		model = super.buildInsertion(id, model);

		model.getId().setCreatedTimestamp(LocalDateTime.now());
		model.setDroppedTimestamp(null);
		model.setApprovedTimestamp(null);
		model.setApprovedBy(null);
		model.setCreatedBy(authService.getOperator());

		return model;
	}

}
