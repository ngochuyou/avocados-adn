/**
 * 
 */
package adn.service.services;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import adn.dao.AbstractRepository;
import adn.dao.parameter.ParamContext;
import adn.model.entities.DepartmentChief;
import adn.model.entities.Personnel;
import adn.model.factory.AuthenticationBasedModelFactory;
import adn.model.factory.AuthenticationBasedModelPropertiesFactory;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Service
public class DepartmentService implements adn.service.internal.Service {

//	private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

	private final SessionFactory sessionFactory;
	private final CRUDServiceImpl crudService;
	private final AbstractRepository repository;

	private final AuthenticationBasedModelFactory modelFactory;
	private final AuthenticationBasedModelPropertiesFactory modelPropertiesFactory;

	@Autowired
	public DepartmentService(
	// @formatter:off
			SessionFactory sessionFactory,
			CRUDServiceImpl crudService,
			AbstractRepository repository,
			AuthenticationBasedModelFactory modelFactory,
			AuthenticationBasedModelPropertiesFactory modelPropertiesFactory) {
		// @formatter:on
		this.sessionFactory = sessionFactory;
		this.crudService = crudService;
		this.repository = repository;
		this.modelFactory = modelFactory;
		this.modelPropertiesFactory = modelPropertiesFactory;
	}

	public Personnel getDepartmentChief(UUID departmentId) {
		// @formatter:off
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Personnel> query = builder.createQuery(Personnel.class);
		Root<DepartmentChief> root  = query.from(DepartmentChief.class);
		// @formatter:off
		query
			.select(root.get("personnel"))
			.where(builder.and(
					builder.equal(root.get("id").get("departmentId"), departmentId),
					builder.isNull(root.get("endDate"))));
		// @formatter:on
		Personnel chief = repository.findOne(query, Personnel.class);

		if (chief == null) {
			return null;
		}

		return chief;
	}

	public Map<String, Object> getDepartmentChief(UUID departmentId, Role role) {
		Personnel chief = getDepartmentChief(departmentId);

		if (chief == null) {
			return null;
		}

		return modelFactory.produce(Personnel.class, getDepartmentChief(departmentId), role);
	}

	public Map<String, Object> getDepartmentChief(UUID departmentId, String[] columns, Role role)
			throws SQLSyntaxErrorException {
		String[] validatedColumns = crudService.getDefaultColumnsOrTranslate(Personnel.class, role, columns);
		// @formatter:off
		String query = String.format("""
				SELECT %s
				FROM DepartmentChief dc
				INNER JOIN Personnel p
					ON dc.personnel.id = p.id
				WHERE dc.department.id=:id AND dc.endDate IS NULL
				""", Stream.of(validatedColumns)
						.map(col -> "p.".concat(col))
						.collect(Collectors.joining(",")));
		// @formatter:on
		Object[] row = repository.findOne(query, Map.of("id", departmentId));

		if (row == null) {
			return null;
		}

		return modelPropertiesFactory.produce(Personnel.class, row, validatedColumns, role);
	}

	public List<Map<String, Object>> getDepartmentChiefs(UUID[] departmentIds, String[] columns, Role role)
			throws SQLSyntaxErrorException {
		String[] validatedColumns = crudService.getDefaultColumnsOrTranslate(Personnel.class, role, columns);
		// @formatter:off
		String query = String.format("""
				SELECT %s
				FROM DepartmentChief dc
				INNER JOIN Personnel p
					ON dc.personnel.id = p.id
				WHERE dc.department.id IN (:ids) AND dc.endDate IS NULL
				""", Stream.of(validatedColumns)
						.map(col -> "p.".concat(col))
						.collect(Collectors.joining(",")));
		// @formatter:on
		List<Object[]> rows = repository.findWithContext(query, Map.of("ids", ParamContext.array(departmentIds)));

		if (rows.isEmpty()) {
			return new ArrayList<>();
		}

		return modelPropertiesFactory.produce(Personnel.class, rows, validatedColumns, role);
	}

	public Long[] countPersonnel(UUID[] departmentIds) {
		String query = """
				SELECT COUNT(*)
				FROM Personnel p
				WHERE p.department.id IN (:ids)
				GROUP BY p.department.id
				""";
		List<Long> countResults = repository.countWithContext(query, Map.of("ids", ParamContext.array(departmentIds)));
		int size;

		if ((size = countResults.size()) == 0) {
			return new Long[0];
		}

		return countResults.toArray(new Long[size]);
	}

	public List<Map<String, Object>> getPersonnelListByDepartmentId(UUID departmentId, String[] requestedColumns,
			Pageable paging, String[] groupByColumns, Role role) throws SQLSyntaxErrorException {
		String[] validateSelectColumns = crudService.getDefaultColumnsOrTranslate(Personnel.class, role,
				requestedColumns);
		// @formatter:off
		String query = String.format("""
				SELECT %s FROM Personnel p
				WHERE p.department.id=:id
					""", Stream.of(validateSelectColumns)
					.map(col -> "p.".concat(col))
					.collect(Collectors.joining(",")));
		// @formatter:on
		query = crudService.resolveGroupByClause(Personnel.class, role, query, groupByColumns);
		query = repository.appendOrderBy(query, paging.getSort());

		List<Object[]> rows = repository.find(query, paging, Map.of("id", departmentId));

		if (rows.isEmpty()) {
			return new ArrayList<>();
		}

		return modelPropertiesFactory.produce(Personnel.class, rows, validateSelectColumns, role);
	}
	
	public UUID getPersonnelDepartmentId(String personnelId) {
		Object[] row = repository.findById(personnelId, Personnel.class, new String[] { "department.id" });
		
		if (row == null) {
			return null;
		}
		
		return (UUID) row[0];
	}

}
