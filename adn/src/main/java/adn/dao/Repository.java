/**
 * 
 */
package adn.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaQuery;

import org.springframework.data.domain.Pageable;

import adn.dao.parameter.ParamContext;
import adn.model.DatabaseInteractionResult;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface Repository {

	<T extends Entity> List<T> fetch(Class<T> type);

	<T extends Entity> List<T> fetch(Class<T> type, Pageable paging);

	<T extends Entity> List<T> fetch(Class<T> type, Pageable paging, String[] groupByColumns);

	<T extends Entity> List<Object[]> fetch(Class<T> type, String[] columns, Pageable paging);

	<T extends Entity> List<Object[]> fetch(Class<T> type, String[] columns, Pageable paging, String[] groupByColumns);

	<T extends Entity> T findById(Serializable id, Class<T> clazz);

	<T extends Entity> Object[] findById(Serializable id, Class<T> clazz, String[] columns);

	<T extends Entity> T findOne(CriteriaQuery<T> query, Class<T> clazz);

	<T extends Entity> T findOne(String query, Class<T> clazz, Map<String, Object> parameters);

	Object[] findOne(String query, Map<String, Object> parameters);

	<T extends Entity> List<T> find(CriteriaQuery<T> query, Class<T> clazz);

	List<Object[]> find(String query, Map<String, Object> parameters);

	List<Object[]> findWithContext(String query, Map<String, ParamContext> parameters);
	
	List<Long> count(String hql, Map<String, Object> params);
	
	List<Long> countWithContext(String hql, Map<String, ParamContext> params);
	
	<T extends Entity, E extends T> DatabaseInteractionResult<E> insert(Serializable id, E model, Class<E> type);

	<T extends Entity, E extends T> DatabaseInteractionResult<E> update(Serializable id, E model, Class<E> type);

}
