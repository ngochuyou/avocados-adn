/**
 * 
 */
package adn.service.services;

import static adn.helpers.EntityUtils.getIdentifier;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import adn.dao.Repository;
import adn.model.DatabaseInteractionResult;
import adn.model.entities.Entity;
import adn.service.entity.builder.EntityBuilder;
import adn.service.entity.builder.EntityBuilderProvider;
import adn.service.internal.CRUDService;

/**
 * @author Ngoc Huy
 *
 */
@Service
@Primary
public class DefaultCRUDService<T extends Entity> implements CRUDService<T> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Repository<Entity> repository;
	private final EntityBuilderProvider entityBuilderProvider;

	@Autowired
	public DefaultCRUDService(Repository<Entity> baseRepository, EntityBuilderProvider entityBuilderProvider) {
		this.repository = baseRepository;
		this.entityBuilderProvider = entityBuilderProvider;
	}

	@Override
	public <E extends T> DatabaseInteractionResult<E> create(E entity, Class<E> type) {
		return create(getIdentifier(entity), entity, type);
	}

	@Override
	public <E extends T> DatabaseInteractionResult<E> update(E entity, Class<E> type) {
		return update(getIdentifier(entity), entity, type);
	}

	@Override
	public <E extends T> DatabaseInteractionResult<E> remove(E entity, Class<E> type) {
		return remove(getIdentifier(entity), entity, type);
	}

	@Override
	public <E extends T> DatabaseInteractionResult<E> create(Serializable id, E entity, Class<E> type) {
		EntityBuilder<E> entityBuilder = entityBuilderProvider.getBuilder(type);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Building entity for creation [%s#%s]", type.getName(), id));
		}

		entityBuilder.insertionBuild(id, entity);

		return repository.insert(id, entity, type);
	}

	@Override
	public <E extends T> DatabaseInteractionResult<E> update(Serializable id, E entity, Class<E> type) {
		EntityBuilder<E> entityBuilder = entityBuilderProvider.getBuilder(type);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Building entity for update [%s#%s]", type.getName(), id));
		}

		entityBuilder.updateBuild(id, entity);

		return repository.update(id, getCurrentSession().load(type, id), type);
	}

	@Override
	public <E extends T> DatabaseInteractionResult<E> remove(Serializable id, E entity, Class<E> type) {
		EntityBuilder<E> entityBuilder = entityBuilderProvider.getBuilder(type);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Building entity for deactivation [%s#%s]", type.getName(), id));
		}

		entityBuilder.deactivationBuild(id, entity);

		return repository.update(id, getCurrentSession().load(type, id), type);
	}

}
