/**
 * 
 */
package adn.service.services;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import adn.dao.generic.Result;
import adn.model.entities.Category;
import adn.service.DomainEntityServiceObserver;
import adn.service.ObservableDomainEntityService;

/**
 * @author Ngoc Huy
 *
 */
@Service
public class CategoryService implements adn.service.internal.Service, ObservableDomainEntityService<Category> {

	private final GenericCRUDServiceImpl crudService;

	private final Map<String, DomainEntityServiceObserver<Category>> observers = new HashMap<>(0);

	public CategoryService(GenericCRUDServiceImpl crudService) {
		super();
		this.crudService = crudService;
	}

	public Result<Category> createCategory(Category category, boolean flushOnFinish) {
		Result<Category> result = crudService.create(category.getId(), category, Category.class, false);

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
