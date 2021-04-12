/**
 * 
 */
package adn.service.resource.metamodel;

import static adn.service.resource.local.ResourceManagerFactoryBuilder.unsupport;

import java.io.Serializable;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.tuple.entity.EntityTuplizer;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceTuplizer extends EntityTuplizer {

	@Override
	default void afterInitialize(Object entity, SharedSessionContractImplementor session) {
		// TODO Auto-generated method stub
		unsupport();
	}

	@Override
	default Object createProxy(Serializable id, SharedSessionContractImplementor session) throws HibernateException {
		// TODO Auto-generated method stub
		unsupport();
		return null;
	}

	@Override
	default String determineConcreteSubclassEntityName(Object entityInstance, SessionFactoryImplementor factory) {
		// TODO Auto-generated method stub
		unsupport();
		return null;
	}

	@Override
	default Serializable getIdentifier(Object entity, SharedSessionContractImplementor session) {
		// TODO Auto-generated method stub
		unsupport();
		return null;
	}

	@Override
	default Object[] getPropertyValuesToInsert(Object entity, @SuppressWarnings("rawtypes") Map mergeMap,
			SharedSessionContractImplementor session) throws HibernateException {
		// TODO Auto-generated method stub
		unsupport();
		return null;
	}

	@Override
	default Object instantiate(Serializable id, SharedSessionContractImplementor session) {
		// TODO Auto-generated method stub
		unsupport();
		return null;
	}

	@Override
	default void resetIdentifier(Object entity, Serializable currentId, Object currentVersion,
			SharedSessionContractImplementor session) {
		// TODO Auto-generated method stub
		unsupport();
	}

	@Override
	default void setIdentifier(Object entity, Serializable id, SharedSessionContractImplementor session) {
		// TODO Auto-generated method stub
		unsupport();
	}

}
