/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceIdentifierGenerator<T> extends IdentifierGenerator {

	@Override
	default Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
		// TODO Auto-generated method stub
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	T generate(ResourceManagerFactory factory, Object owner);

}
