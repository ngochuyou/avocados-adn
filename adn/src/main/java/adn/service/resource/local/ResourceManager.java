/**
 * 
 */
package adn.service.resource.local;

import static adn.service.resource.local.ResourceManagerFactory.unsupport;

import java.util.List;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceManager extends EntityManager {

	ResourceManagerFactory getResourceManagerFactory();

	void setRollbackOnly();

	boolean isRollbackOnly();

	ResourceContext getResourceContext();

	ActionQueue getActionQueue();

	@Override
	@Deprecated
	default <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
		unsupport();
		return null;
	};

	@Override
	@Deprecated
	default EntityGraph<?> createEntityGraph(String graphName) {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default Query createNamedQuery(String name) {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default Query createNativeQuery(String sqlString) {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default Query createNativeQuery(String sqlString, @SuppressWarnings("rawtypes") Class resultClass) {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default Query createNativeQuery(String sqlString, String resultSetMapping) {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default Query createQuery(@SuppressWarnings("rawtypes") CriteriaDelete deleteQuery) {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default Query createQuery(@SuppressWarnings("rawtypes") CriteriaUpdate updateQuery) {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default Query createQuery(String qlString) {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default StoredProcedureQuery createStoredProcedureQuery(String procedureName,
			@SuppressWarnings("rawtypes") Class... resultClasses) {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default CriteriaBuilder getCriteriaBuilder() {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default EntityGraph<?> getEntityGraph(String graphName) {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
		unsupport();
		return null;
	}

	@Override
	@Deprecated
	default Metamodel getMetamodel() {
		unsupport();
		return null;
	}

}
