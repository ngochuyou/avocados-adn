/**
 * 
 */
package adn.service.resource.local;

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
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	};

	@Override
	@Deprecated
	default EntityGraph<?> createEntityGraph(String graphName) {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default Query createNamedQuery(String name) {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default Query createNativeQuery(String sqlString) {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default Query createNativeQuery(String sqlString, @SuppressWarnings("rawtypes") Class resultClass) {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default Query createNativeQuery(String sqlString, String resultSetMapping) {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default Query createQuery(@SuppressWarnings("rawtypes") CriteriaDelete deleteQuery) {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default Query createQuery(@SuppressWarnings("rawtypes") CriteriaUpdate updateQuery) {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default Query createQuery(String qlString) {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default StoredProcedureQuery createStoredProcedureQuery(String procedureName,
			@SuppressWarnings("rawtypes") Class... resultClasses) {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default CriteriaBuilder getCriteriaBuilder() {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default EntityGraph<?> getEntityGraph(String graphName) {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	@Deprecated
	default Metamodel getMetamodel() {
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

}
