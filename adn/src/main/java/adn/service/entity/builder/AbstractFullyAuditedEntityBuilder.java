/**
 * 
 */
package adn.service.entity.builder;

import static java.time.LocalDateTime.now;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;

import adn.helpers.StringHelper;
import adn.model.entities.FullyAuditedEntity;
import adn.service.services.AuthenticationService;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractFullyAuditedEntityBuilder<T extends FullyAuditedEntity<?>>
		extends AbstractPermanentEntityBuilder<T> {

	private final AuthenticationService authService;

	@Autowired
	public AbstractFullyAuditedEntityBuilder(AuthenticationService authService) {
		super();
		this.authService = authService;
	}

	protected <E extends T> E mandatoryBuild(E target, E model) {
		target = super.mandatoryBuild(target, model);

		target.setName(StringHelper.normalizeString(model.getName()));

		return target;
	}

	@Override
	public <E extends T> E buildInsertion(Serializable id, E model) {
		model = super.buildInsertion(id, model);

		model.setCreatedBy(authService.getOperator());
		model.setCreatedDate(now());
		model.setLastModifiedBy(authService.getOperator());
		model.setLastModifiedDate(now());
		model.setApprovedBy(null);
		model.setApprovedTimestamp(null);

		return model;
	}

	@Override
	public <E extends T> E buildUpdate(Serializable id, E model, E persistence) {
		persistence = super.buildUpdate(id, model, persistence);

		persistence.setLastModifiedBy(authService.getOperator());
		persistence.setLastModifiedDate(now());

		return persistence;
	}

}
