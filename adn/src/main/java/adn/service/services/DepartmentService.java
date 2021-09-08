/**
 * 
 */
package adn.service.services;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import adn.model.entities.Personnel;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;

/**
 * @author Ngoc Huy
 *
 */
@Service
public class DepartmentService implements adn.service.internal.Service {

//	private final SessionFactory sessionFactory;
	private final GenericCRUDServiceImpl crudService;

	// @formatter:off
	@Autowired
	public DepartmentService(/*SessionFactory sessionFactory, */GenericCRUDServiceImpl crudService) {
//		this.sessionFactory = sessionFactory;
		this.crudService = crudService;
	}
	// @formatter:on

	public Personnel getDepartmentChief(UUID departmentId) {
//		// @formatter:off
//		Session session = sessionFactory.getCurrentSession();
//		CriteriaBuilder builder = session.getCriteriaBuilder();
//		CriteriaQuery<Personnel> query = builder.createQuery(Personnel.class);
//		Root<DepartmentChief> root  = query.from(DepartmentChief.class);
//		// @formatter:off
//		query
//			.select(root.get("personnel"))
//			.where(builder.and(
//					builder.equal(root.get("id").get("departmentId"), departmentId),
//					builder.isNull(root.get("endDate"))));
//		// @formatter:on
//		Personnel chief = crudService.repository.findOne(query, Personnel.class);
//
//		if (chief == null) {
//			return null;
//		}
//
//		return chief;
		return null;
	}

	public Map<String, Object> getDepartmentChief(UUID departmentId, Credential credential)
			throws UnauthorizedCredential {
		Personnel chief = getDepartmentChief(departmentId);

		if (chief == null) {
			return null;
		}

		return crudService.dynamicMapModelFactory.producePojo(chief, null, credential);
	}

	public Map<String, Object> getDepartmentChief(UUID departmentId, Collection<String> columns, Credential credential)
			throws NoSuchFieldException, UnauthorizedCredential {
//		SourceMetadata<Personnel> metadata = crudService.optionallyValidate(Personnel.class, credential,
//				unknownArray(Personnel.class, list(columns)));
//		// @formatter:off
//		String query = String.format("""
//				SELECT %s
//				FROM DepartmentChief dc
//				INNER JOIN Personnel p
//					ON dc.personnel.id = p.id
//				WHERE dc.department.id=:id AND dc.endDate IS NULL
//				""", metadata.getColumns().stream()
//						.map(col -> "p.".concat(col))
//						.collect(Collectors.joining(",")));
//		// @formatter:on
//		Object[] row = crudService.repository.findOne(query, Map.of("id", departmentId));
//
//		if (row == null) {
//			return null;
//		}
//
//		return crudService.resolveReadResult(Personnel.class, row, credential, metadata);
		return null;
	}

	public List<Map<String, Object>> getDepartmentChiefs(UUID[] departmentIds, Collection<String> columns,
			Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
//		SourceMetadata<Personnel> metadata = crudService.optionallyValidate(Personnel.class, credential,
//				unknownArrayCollection(Personnel.class, list(columns)));
//		// @formatter:off
//		String query = String.format("""
//				SELECT %s
//				FROM DepartmentChief dc
//				INNER JOIN Personnel p
//					ON dc.personnel.id = p.id
//				WHERE dc.department.id IN (:ids) AND dc.endDate IS NULL
//				""", metadata.getColumns().stream()
//						.map(col -> "p." + col)
//						.collect(Collectors.joining(",")));
//		// @formatter:on
//		List<?> rows = crudService.repository.findWithContext(query, Map.of("ids", ParamContext.array(departmentIds)));
//
//		if (rows.isEmpty()) {
//			return new ArrayList<>();
//		}
//
//		return crudService.resolveReadResults(Personnel.class, rows, credential, metadata);
		return null;
	}

	public Long[] countPersonnel(UUID[] departmentIds) {
//		String query = """
//				SELECT COUNT(*)
//				FROM Personnel p
//				WHERE p.department.id IN (:ids)
//				GROUP BY p.department.id
//				""";
//		List<Long> countResults = crudService.repository.countWithContext(query,
//				Map.of("ids", ParamContext.array(departmentIds)));
//		int size;
//
//		if ((size = countResults.size()) == 0) {
//			return new Long[0];
//		}
//
//		return countResults.toArray(new Long[size]);
		return null;
	}

	public List<Map<String, Object>> getPersonnelListByDepartmentId(UUID departmentId, Collection<String> columns,
			Pageable paging, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
//		SourceMetadata<Personnel> metadata = crudService.optionallyValidate(Personnel.class, credential,
//				unknownArrayCollection(Personnel.class, list(columns)));
//		// @formatter:off
//		String query = String.format("""
//				SELECT %s FROM Personnel p
//				WHERE p.department.id=:id
//					""", metadata.getColumns().stream()
//					.map(col -> "p.".concat(col))
//					.collect(Collectors.joining(",")));
//		// @formatter:on
//		query = crudService.repository.appendOrderBy(query, paging.getSort());
//
//		List<?> rows = crudService.repository.find(query, paging, Map.of("id", departmentId));
//
//		if (rows.isEmpty()) {
//			return new ArrayList<>();
//		}
//
//		return crudService.resolveReadResults(Personnel.class, rows, credential, metadata);
		return null;
	}

	public UUID getPersonnelDepartmentId(String personnelId) {
		Optional<Object[]> optional = crudService.repository.findById(Personnel.class, personnelId,
				Arrays.asList("department.id"));

		if (optional.isEmpty()) {
			return null;
		}

		return (UUID) optional.get()[0];
	}

}
