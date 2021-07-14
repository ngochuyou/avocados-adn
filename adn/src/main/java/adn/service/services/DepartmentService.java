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
import org.springframework.stereotype.Service;

import adn.dao.parameter.ParamContext;
import adn.model.entities.DepartmentChief;
import adn.model.entities.Personnel;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Service
public class DepartmentService extends DefaultCRUDService {

//	private static final Logger logger = LoggerFactory.getLogger(DepartmentService.class);

	private final SessionFactory sessionFactory;

	@Autowired
	public DepartmentService(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
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

		return authenticationBasedModelFactory.produce(Personnel.class, getDepartmentChief(departmentId), role);
	}

	public Map<String, Object> getDepartmentChief(UUID departmentId, String[] columns, Role role)
			throws SQLSyntaxErrorException {
		String[] validatedColumns = getDefaultColumnsOrTranslate(Personnel.class, role, columns);
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

		return authenticationBasedModelPropertiesFactory.produce(Personnel.class, row, validatedColumns, role);
	}

	public List<Map<String, Object>> getDepartmentChiefs(UUID[] departmentIds, String[] columns, Role role)
			throws SQLSyntaxErrorException {
		String[] validatedColumns = getDefaultColumnsOrTranslate(Personnel.class, role, columns);
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

		return authenticationBasedModelPropertiesFactory.produce(Personnel.class, rows, validatedColumns, role);
	}

}
