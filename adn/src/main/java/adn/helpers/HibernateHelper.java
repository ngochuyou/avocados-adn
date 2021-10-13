/**
 * 
 */
package adn.helpers;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Tuple;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.StaleStateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.id.CompositeNestedGeneratedValueGenerator;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.query.NativeQuery;

import adn.application.context.ContextProvider;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public class HibernateHelper {

	public static final String UNKNOWN_COLUMNS = "Unknown columns found";
	public static final String HIBERNATE_SEQUENCE_TABLE_NAME = "id_generators";
	public static final String HIBERNATE_SEQUENCE_PK = "sequence_name";
	public static final String HIBERNATE_SEQUENCE_VAL = "next_val";
	public static final String ID_GENERATOR_UPDATE_FAILED_TEMPLATE = "Unable to update next value for id generator of table %s";

	private HibernateHelper() {}

	public static SessionFactoryImplementor getSessionFactory() {
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

	public static <T extends Entity> boolean isIdentifierAutoGeneratedButNotEmbedded(Class<T> type) {
		// @formatter:off
		IdentifierGenerator generator = getEntityPersister(type).getEntityMetamodel()
				.getIdentifierProperty().getIdentifierGenerator();
		
		return generator != null && !(generator instanceof CompositeNestedGeneratedValueGenerator);
		// @formatter:on
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
		return tuples.stream().map(Tuple::toArray).collect(Collectors.toList());
	}

	public static void useManualSession() {
		useManualSession(ContextProvider.getCurrentSession());
	}

	public static void useManualSession(Session session) {
		session.setHibernateFlushMode(FlushMode.MANUAL);
	}

	// @formatter:off
	private static final String GENERATED_ID_SELECT_TEMPLATE = String.format("SELECT %s FROM %s WHERE %s = '%s' FOR UPDATE",
			HIBERNATE_SEQUENCE_VAL, HIBERNATE_SEQUENCE_TABLE_NAME, HIBERNATE_SEQUENCE_PK, "%s");
	private static final String GENERATED_ID_PARAMETER_NAME = "nextVal";
	private static final String TABLE_NAME_PARAMETER_NAME = "tableName";
	private static final String GENERATED_ID_UPDATE_TEMPLATE = String.format("UPDATE %s SET %s = :%s WHERE %s = :%s",
			HIBERNATE_SEQUENCE_TABLE_NAME, HIBERNATE_SEQUENCE_VAL, GENERATED_ID_PARAMETER_NAME,
			HIBERNATE_SEQUENCE_PK, TABLE_NAME_PARAMETER_NAME);
	// @formatter:on
	public static <T extends Entity> BigInteger getNextAutoIncrementedValue(Class<T> type) {
		AbstractEntityPersister persister = (AbstractEntityPersister) getEntityPersister(type);
		@SuppressWarnings("unchecked")
		NativeQuery<BigInteger> query = ContextProvider.getCurrentSession()
				.createNativeQuery(String.format(GENERATED_ID_SELECT_TEMPLATE, persister.getTableName()));

		return query.getResultStream().findFirst().orElseThrow(() -> new StaleStateException(
				String.format("Unable to get AUTO_INCREMENT identifier of entity type: [%s]", type.getName())));
	}

	public static <T extends Entity> int updateNextAutoIncrementedValue(Class<T> type, BigInteger nextVal) {
		AbstractEntityPersister persister = (AbstractEntityPersister) getEntityPersister(type);
		@SuppressWarnings("unchecked")
		NativeQuery<Integer> query = ContextProvider.getCurrentSession()
				.createNativeQuery(String.format(GENERATED_ID_UPDATE_TEMPLATE, nextVal, persister.getTableName()));
		
		return query.executeUpdate();
	}

}
