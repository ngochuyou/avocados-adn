/**
 * 
 */
package adn.service.services;

import static adn.helpers.CollectionHelper.from;
import static adn.helpers.CollectionHelper.list;
import static adn.model.factory.authentication.dynamicmap.SourceMetadataFactory.unknownArray;
import static adn.model.factory.authentication.dynamicmap.SourceMetadataFactory.unknownArrayCollection;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import adn.application.context.ContextProvider;
import adn.application.context.builders.DepartmentScopeContext;
import adn.dao.generic.ParamContext;
import adn.helpers.CollectionHelper;
import adn.model.entities.DepartmentChief;
import adn.model.entities.Personnel;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.security.PersonnelDetails;
import adn.security.UserDetailsImpl;

/**
 * @author Ngoc Huy
 *
 */
@Service
public class DepartmentService implements adn.service.internal.Service {

	private final SessionFactory sessionFactory;
	private final GenericCRUDService crudService;

	// @formatter:off
	@Autowired
	public DepartmentService(SessionFactory sessionFactory, GenericCRUDService crudService) {
		this.sessionFactory = sessionFactory;
		this.crudService = crudService;
	}
	// @formatter:on
	public UUID getPrincipalDepartment() {
		UserDetailsImpl userDetails = ContextProvider.getPrincipal();

		if (!(userDetails instanceof PersonnelDetails)) {
			return DepartmentScopeContext.unknown();
		}

		return ((PersonnelDetails) userDetails).getDepartmentId();
	}

	public UUID assertSaleDepartment() {
		UUID principalDepartment = getPrincipalDepartment();

		DepartmentScopeContext.assertDepartment(principalDepartment, DepartmentScopeContext.sale());

		return principalDepartment;
	}

	public void assertStockDepartment() {
		DepartmentScopeContext.assertDepartment(getPrincipalDepartment(), DepartmentScopeContext.stock());
	}

	public void assertPersonnelDepartment() {
		DepartmentScopeContext.assertDepartment(getPrincipalDepartment(), DepartmentScopeContext.personnel());
	}

	public boolean isPersonnelDepartment() {
		return getPrincipalDepartment().equals(DepartmentScopeContext.personnel());
	}

	public boolean isPersonnelDepartment(UUID requestedDepartmentId) {
		return requestedDepartmentId == DepartmentScopeContext.personnel();
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
		Personnel chief = crudService.repository.findOne(query, Personnel.class);

		if (chief == null) {
			return null;
		}

		return chief;
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
		Collection<String> validatedColumns = crudService.getDefaultColumns(Personnel.class, credential, columns);
		// @formatter:off
		String query = String.format("""
				SELECT %s
				FROM DepartmentChief dc
				INNER JOIN Personnel p
					ON dc.personnel.id = p.id
				WHERE dc.department.id=:id AND dc.endDate IS NULL
				""", validatedColumns.stream()
						.map(col -> "p.".concat(col))
						.collect(Collectors.joining(",")));
		// @formatter:on
		Object[] row = crudService.repository.findOne(query, Map.of("id", departmentId));

		if (row == null) {
			return null;
		}

		return crudService.dynamicMapModelFactory.produce(row,
				unknownArray(Personnel.class, CollectionHelper.list(validatedColumns)), credential);
	}

	public List<Map<String, Object>> getDepartmentChiefs(UUID[] departmentIds, Collection<String> columns,
			Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		Collection<String> validatedColumns = crudService.getDefaultColumns(Personnel.class, credential, columns);
		// @formatter:off
		String query = String.format("""
				SELECT %s
				FROM DepartmentChief dc
				INNER JOIN Personnel p
					ON dc.personnel.id = p.id
				WHERE dc.department.id IN (:ids) AND dc.endDate IS NULL
				""", validatedColumns.stream()
						.map(col -> "p." + col)
						.collect(Collectors.joining(",")));
		// @formatter:on
		List<?> rows = crudService.repository.findWithContext(query, Map.of("ids", ParamContext.array(departmentIds)));

		return crudService.resolveReadResults(Personnel.class, rows, from(validatedColumns), credential,
				unknownArrayCollection(Personnel.class, list(validatedColumns)));
	}

	public Long[] countPersonnel(UUID[] departmentIds) {
		String query = """
				SELECT COUNT(*)
				FROM Personnel p
				WHERE p.department.id IN (:ids)
				GROUP BY p.department.id
				""";
		List<Long> countResults = crudService.repository.countWithContext(query,
				Map.of("ids", ParamContext.array(departmentIds)));
		int size;

		if ((size = countResults.size()) == 0) {
			return new Long[0];
		}

		return countResults.toArray(new Long[size]);
	}

	public List<Map<String, Object>> getPersonnelListByDepartmentId(UUID departmentId, Collection<String> columns,
			Pageable paging, Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		Collection<String> validatedColumns = crudService.getDefaultColumns(Personnel.class, credential, columns);
		// @formatter:off
		String query = String.format("""
				SELECT %s FROM Personnel p
				WHERE p.department.id=:id
					""", validatedColumns.stream()
					.map(col -> "p.".concat(col))
					.collect(Collectors.joining(",")));
		// @formatter:on
		query = crudService.repository.appendOrderBy(query, paging.getSort());

		List<?> rows = crudService.repository.find(query, paging, Map.of("id", departmentId));

		return crudService.resolveReadResults(Personnel.class, rows, from(validatedColumns), credential,
				unknownArrayCollection(Personnel.class, list(columns)));
	}

	public UUID getPersonnelDepartmentId(String personnelId) {
		Object[] row = crudService.repository.findById(personnelId, Personnel.class, new String[] { "department.id" });

		if (row == null) {
			return null;
		}

		return (UUID) row[0];
	}

}
