/**
 * 
 */
package adn.service.services;

import static adn.helpers.CollectionHelper.list;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import adn.dao.generic.GenericRepository;
import adn.helpers.HibernateHelper;
import adn.model.entities.PermanentEntity;
import adn.model.entities.metadata._Factor;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.SourceMetadata;
import adn.model.factory.authentication.dynamicmap.SourceMetadataFactory;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.service.internal.Service;

/**
 * @author Ngoc Huy
 *
 */
@org.springframework.stereotype.Service
public class GenericPermanentEntityService implements Service {

	private final GenericRepository genericRepository;
	private final GenericCRUDServiceImpl crudService;

	public GenericPermanentEntityService(GenericRepository repository, GenericCRUDServiceImpl genericCrudService) {
		super();
		this.genericRepository = repository;
		this.crudService = genericCrudService;
	}

	public <T extends PermanentEntity> Long count(Class<T> type) {
		return genericRepository.count(type, isActive(type));
	}

	@SuppressWarnings("serial")
	public <T extends PermanentEntity> Map<String, Object> readById(Class<T> type, Serializable id,
			Collection<String> columns, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		SourceMetadata<T> metadata = crudService.optionallyValidate(type, credential,
				SourceMetadataFactory.unknownArray(type, list(columns)));
		Optional<Object[]> optional = genericRepository.findOne(type, metadata.getColumns(),
				isActive(type).and(new Specification<T>() {
					@Override
					public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
						return builder.equal(root.get(HibernateHelper.getIdentifierPropertyName(type)), id);
					}
				}));

		if (optional.isEmpty()) {
			return null;
		}

		return crudService.resolveReadResult(type, optional.get(), credential, metadata);
	}

	public <T extends PermanentEntity> List<Map<String, Object>> readAll(Class<T> type, Collection<String> columns,
			Pageable paging, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		return readAll(type, null, columns, paging, credential);
	}

	public <T extends PermanentEntity> List<Map<String, Object>> readAll(Class<T> type, Specification<T> spec,
			Collection<String> columns, Pageable paging, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential {
		SourceMetadata<T> metadata = crudService.optionallyValidate(type, credential,
				SourceMetadataFactory.unknownArrayCollection(type, list(columns)));
		List<Object[]> rows = genericRepository.findAll(type, metadata.getColumns(), isActive(type).and(spec), paging);

		if (rows.isEmpty()) {
			return new ArrayList<>();
		}

		return crudService.resolveReadResults(type, rows, credential, metadata);
	}

	@SuppressWarnings("serial")
	static <T extends PermanentEntity, E extends T> Specification<E> isActive(Class<E> type) {
		return new Specification<E>() {
			@Override
			public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				return builder.isTrue(root.get(_Factor.active));
			}
		};
	}

}
