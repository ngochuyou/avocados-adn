/**
 * 
 */
package adn.service.services;

import static adn.helpers.CollectionHelper.from;
import static adn.helpers.HibernateHelper.selectColumns;
import static adn.helpers.HibernateHelper.toRows;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;

import adn.model.entities.Product;
import adn.model.entities.StockDetail;
import adn.service.internal.Role;
import adn.service.internal.Service;

/**
 * @author Ngoc Huy
 *
 */
@org.springframework.stereotype.Service
public class StockDetailService implements Service {

	private final GenericCRUDService crudService;
	private static final Collection<String> FETCHED_COLUMNS = Arrays.asList(StockDetail.ID_FIELD_NAME,
			StockDetail.SIZE_FIELD_NAME, StockDetail.NUMERIC_SIZE_FIELD_NAME, StockDetail.COLOR_FIELD_NAME,
			StockDetail.MATERIAL_FIELD_NAME, StockDetail.STATUS_FIELD_NAME, StockDetail.DESCRIPTION_FIELD_NAME);

	@Autowired
	public StockDetailService(GenericCRUDService crudService) {
		super();
		this.crudService = crudService;
	}

	public List<Map<String, Object>> readActiveOnly(Serializable productId, Collection<String> requestedColumns,
			Role principalRole) throws NoSuchFieldException {
		Collection<String> validatedColumns = requestedColumns.isEmpty() ? FETCHED_COLUMNS
				: crudService.getDefaultColumns(StockDetail.class, principalRole, requestedColumns);
		Session session = crudService.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createQuery(Tuple.class);
		Root<StockDetail> root = criteriaQuery.from(StockDetail.class);

		criteriaQuery = selectColumns(criteriaQuery, root, validatedColumns)
				.where(builder.and(builder.equal(root.get(StockDetail.ACTIVE_FIELD_NAME), Boolean.TRUE),
						builder.equal(root.get(StockDetail.PRODUCT_FIELD_NAME).get(Product.ID_FIELD_NAME), productId)));

		Query<Tuple> jpql = session.createQuery(criteriaQuery);

		return crudService.resolveReadResults(StockDetail.class, toRows(jpql.list()), from(validatedColumns),
				principalRole);
	}

}
