/**
 * 
 */
package adn.service.services;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.hibernate.LockMode;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import adn.application.Result;
import adn.application.context.ContextProvider;
import adn.helpers.Utils;
import adn.model.entities.ApprovableResource;
import adn.service.internal.GenericService;

/**
 * @author Ngoc Huy
 *
 */
@Service
public class GenericServiceImpl implements GenericService {

	private final AuthenticationService authService;
	private final GenericCRUDServiceImpl genericCrudService;

	private static final String ALREADY_APPROVED_TEMPLATE = "Resource was already approved on %s";

	@Autowired
	public GenericServiceImpl(AuthenticationService authService, GenericCRUDServiceImpl crudService) {
		super();
		this.authService = authService;
		this.genericCrudService = crudService;
	}

	@Override
	public <T extends ApprovableResource, ID extends Serializable> Result<T> approve(Class<T> type, ID resourceId,
			boolean flushOnFinish) {
		Session session = ContextProvider.getCurrentSession();
		T persistence = session.load(type, resourceId, LockMode.PESSIMISTIC_WRITE);

		if (persistence.getApprovedTimestamp() != null) {
			return Result.bad(
					String.format(ALREADY_APPROVED_TEMPLATE, Utils.localDateTime(persistence.getApprovedTimestamp())));
		}

		persistence.setApprovedBy(authService.getHead());
		persistence.setApprovedTimestamp(LocalDateTime.now());

		return genericCrudService.finish(session, Result.ok(persistence), flushOnFinish);
	}

}
