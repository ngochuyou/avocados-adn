/**
 * 
 */
package adn.service.services;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import adn.dao.DatabaseInteractionResult;
import adn.model.entities.Category;
import adn.model.factory.DepartmentBasedModelPropertiesFactory;
import adn.service.DomainEntityServiceObserver;
import adn.service.ObservableDomainEntityService;
import adn.service.internal.Service;

/**
 * @author Ngoc Huy
 *
 */
@org.springframework.stereotype.Service
public class ProductService implements Service, ObservableDomainEntityService<Category> {

	@SuppressWarnings("unused")
	private final DepartmentBasedModelPropertiesFactory departmentBasedModelFactory;
	private final CRUDServiceImpl crudService;

	private final Map<String, DomainEntityServiceObserver<Category>> observers = new HashMap<>(0);

	@Autowired
	public ProductService(CRUDServiceImpl crudService,
			DepartmentBasedModelPropertiesFactory departmentBasedModelFactory) {
		this.crudService = crudService;
		this.departmentBasedModelFactory = departmentBasedModelFactory;
	}

	public DatabaseInteractionResult<Category> createCategory(Category category, boolean flushOnFinish) {
		// to avoid name uniqueness violation when callers
		// explicitly set an existing id into the model.
		// This will cause the Specification check on name uniqueness to pass
		// while it shouldn't have
		category.setId(null);

		DatabaseInteractionResult<Category> result = crudService.create(category.getId(), category, Category.class,
				false);

		if (result.isOk()) {
			observers.values().forEach(observer -> observer.notifyCreation(category));
		}

		return crudService.finish(crudService.getCurrentSession(), result, flushOnFinish);
	}

	@Override
	public void register(DomainEntityServiceObserver<Category> observer) {
		final Logger logger = LoggerFactory.getLogger(this.getClass());

		if (observers.containsKey(observer.getId())) {
			logger.trace(String.format("Ignoring existing observer [%s], id: [%s]", observer.getClass().getName(),
					observer.getId()));
			return;
		}

		logger.trace(String.format("Registering new observer [%s], id: [%s]", observer.getClass().getName(),
				observer.getId()));
		observers.put(observer.getId(), observer);
	}

}
