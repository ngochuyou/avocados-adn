/**
 * 
 */
package adn.service.services;

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

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;

import adn.helpers.CollectionHelper;
import adn.model.entities.Item;
import adn.model.entities.metadata._Item;
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

	private final GenericCRUDServiceImpl crudService;
	private static final List<String> FETCHED_COLUMNS = Arrays.asList(_Item.id,
			_Item.namedSize, _Item.numericSize, _Item.color, _Item.status,
			_Item.note);

	@Autowired
	public StockDetailService(GenericCRUDServiceImpl crudService) {
		super();
		this.crudService = crudService;
	}

	public List<Map<String, Object>> readActiveOnly(Serializable productId, Collection<String> requestedColumns,
			Credential credential) throws NoSuchFieldException, UnauthorizedCredential {
		SourceMetadata<Item> metadata = requestedColumns.isEmpty()
				? unknownArrayCollection(Item.class, FETCHED_COLUMNS)
				: crudService.optionallyValidate(Item.class, credential,
						unknownArrayCollection(Item.class, CollectionHelper.list(requestedColumns)));
		Session session = crudService.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> criteriaQuery = builder.createQuery(Tuple.class);
//		Root<StockDetail> root = criteriaQuery.from(StockDetail.class);

//		criteriaQuery = selectColumns(criteriaQuery, root, metadata.getColumns())
//				.where(builder.and(builder.equal(root.get(StockDetail.ACTIVE_FIELD_NAME), Boolean.TRUE),
//						builder.equal(root.get(StockDetail.PRODUCT_FIELD_NAME).get(_Product.id), productId)));

		Query<Tuple> jpql = session.createQuery(criteriaQuery);
		List<Tuple> tuples = jpql.list();

		if (tuples.isEmpty()) {
			return new ArrayList<>();
		}

		return crudService.resolveReadResults(Item.class, toRows(tuples), credential, metadata);
	}

}
