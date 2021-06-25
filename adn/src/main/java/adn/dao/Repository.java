/**
 * 
 */
package adn.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.criteria.CriteriaQuery;

import adn.model.DatabaseInteractionResult;

/**
 * @author Ngoc Huy
 *
 */
public interface Repository<T> {

	<E extends T> E findById(Serializable id, Class<E> clazz);

	<E extends T> E findOne(CriteriaQuery<E> query, Class<E> clazz);

	<E extends T> List<E> find(CriteriaQuery<E> query, Class<E> clazz);

	<E extends T> DatabaseInteractionResult<E> insert(E model, Class<E> type);

	<E extends T> DatabaseInteractionResult<E> update(E model, Class<E> type);

}
