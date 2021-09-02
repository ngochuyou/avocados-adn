/**
 * 
 */
package adn.service.services;

import static adn.helpers.HibernateHelper.selectColumns;
import static adn.helpers.HibernateHelper.toRows;
import static adn.model.factory.authentication.dynamicmap.SourceMetadataFactory.unknownArrayCollection;

import java.io.Serializable;
import java.util.ArrayList;
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

import adn.helpers.CollectionHelper;
import adn.model.entities.StockDetail;
import adn.model.entities.metadata._Product;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.SourceMetadata;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.service.internal.Service;

/**
 * @author Ngoc Huy
 *
 */
@org.springframework.stereotype.Service
public class StockDetailService implements Service {

	private final GenericCRUDService crudService;
	private static final List<String> FETCHED_COLUMNS = Arrays.asList(StockDetail.ID_FIELD_NAME,
			StockDetail.SIZE_FIELD_NAME, StockDetail.NUMERIC_SIZE_FIELD_NAME, StockDetail.COLOR_FIELD_NAME,
			StockDetail.MATERIAL_FIELD_NAME, StockDetail.STATUS_FIELD_NAME, StockDetail.DESCRIPTION_FIELD_NAME);

	@Autowired
	public StockDetailService(GenericCRUDService crudService) {
		super();
		this.crudService = crudService;
	}

	public List<Map<String, Object>> readActiveOnly(Serializable productId, Collection<String> requestedColumns,
			Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		SourceMetadata<StockDetail> metadata = requestedColumns.isEmpty()
				? unknownArrayCollection(StockDetail.class, FETCHED_COLUMNS)
				: crudService.getDefaultColumns(StockDetail.class, credential,
						unknownArrayCollection(StockDetail.class, CollectionHelper.list(requestedColumns)));
		Session session = crudService.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createQuery(Tuple.class);
		Root<StockDetail> root = criteriaQuery.from(StockDetail.class);

		criteriaQuery = selectColumns(criteriaQuery, root, metadata.getColumns())
				.where(builder.and(builder.equal(root.get(StockDetail.ACTIVE_FIELD_NAME), Boolean.TRUE),
						builder.equal(root.get(StockDetail.PRODUCT_FIELD_NAME).get(_Product.id), productId)));

		Query<Tuple> jpql = session.createQuery(criteriaQuery);
		List<Tuple> tuples = jpql.list();

		if (tuples.isEmpty()) {
			return new ArrayList<>();
		}

		return crudService.resolveReadResults(StockDetail.class, toRows(tuples), credential, metadata);
	}

}
