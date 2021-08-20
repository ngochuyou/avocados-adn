/**
 * 
 */
package adn.helpers;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.proxy.HibernateProxy;

import adn.application.context.ContextProvider;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public class HibernateHelper {

	public static final String UNKNOWN_COLUMNS = "Unknown columns found";

	private HibernateHelper() {}

	private static SessionFactoryImplementor getSessionFactory() {
		return ContextProvider.getBean(SessionFactoryImplementor.class);
	}

	public static <T extends Entity> String getEntityName(Class<T> type) {
		return getEntityPersister(type).getEntityName();
	}

	public static <T extends Entity> EntityPersister getEntityPersister(Class<T> type) {
		return getSessionFactory().getMetamodel().entityPersister(type);
	}

	public static <T extends Entity> Serializable getIdentifier(T entity) {
		Class<? extends Entity> entityType = getPersistentClass(entity);
		// @formatter:off
		// session arg is not required, at least if the Session is instance of
		// org.hibernate.internal.SessionImpl
		return getEntityPersister(entityType).getIdentifier(entity, null);
		// @formatter:on
	}

	public static <T extends Entity> String getDiscriminatorColumnName(Class<T> type) {
		return ((SingleTableEntityPersister) getEntityPersister(type)).getDiscriminatorColumnName();
	}

	public static <T extends Entity> String getIdentifierPropertyName(Class<T> type) {
		return getEntityPersister(type).getIdentifierPropertyName();
	}

	public static <T extends Entity> boolean isIdentifierAutoGenerated(Class<T> type) {
		// @formatter:off
		return getEntityPersister(type).getEntityMetamodel()
				.getIdentifierProperty().getIdentifierGenerator() != null;
		// @formatter:on
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> getPersistentClass(T instance) {
		return instance instanceof HibernateProxy
				? ((HibernateProxy) instance).getHibernateLazyInitializer().getPersistentClass()
				: (Class<T>) instance.getClass();
	}

	public static List<Object[]> toRows(List<Tuple> tuples) {
		return tuples.stream().map(row -> row.toArray()).collect(Collectors.toList());
	}

	public static <E, R> CriteriaQuery<R> selectColumns(CriteriaQuery<R> query, Root<E> root, Collection<String> columns)
			throws NoSuchFieldException {
		try {
			query.multiselect(columns.stream().map(col -> root.get(col)).collect(Collectors.toList()));
		} catch (IllegalArgumentException iae) {
			throw new NoSuchFieldException(UNKNOWN_COLUMNS);
		}

		return query;
	}

}