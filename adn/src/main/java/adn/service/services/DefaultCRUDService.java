/**
 * 
 */
package adn.service.services;

import static adn.helpers.EntityUtils.getIdentifier;

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

	private final Repository<Entity> repository;
	private final EntityBuilderProvider entityBuilderProvider;

	@Autowired
	public DefaultCRUDService(Repository<Entity> baseRepository, EntityBuilderProvider entityBuilderProvider) {
		this.repository = baseRepository;
		this.entityBuilderProvider = entityBuilderProvider;
	}

	@Override
	public <E extends T> DatabaseInteractionResult<E> create(E entity, Class<E> type) {
		EntityBuilder<E> entityBuilder = entityBuilderProvider.getBuilder(type);

		entity = entityBuilder.insertionBuild(entity);

		return repository.insert(entity, type);
	}

	@Override
	public <E extends T> DatabaseInteractionResult<E> update(E entity, Class<E> type) {
		EntityBuilder<E> entityBuilder = entityBuilderProvider.getBuilder(type);

		entityBuilder.updateBuild(entity);

		return repository.update(getCurrentSession().load(type, getIdentifier(entity)), type);
	}

	@Override
	public <E extends T> DatabaseInteractionResult<E> remove(E entity, Class<E> type) {
		EntityBuilder<E> entityBuilder = entityBuilderProvider.getBuilder(type);

		entityBuilder.deactivationBuild(entity);

		return repository.update(getCurrentSession().load(type, getIdentifier(entity)), type);
	}

}
