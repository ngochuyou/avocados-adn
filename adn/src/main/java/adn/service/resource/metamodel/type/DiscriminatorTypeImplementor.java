/**
 * 
 */
package adn.service.resource.metamodel.type;

import org.hibernate.dialect.Dialect;
import org.hibernate.type.DiscriminatorType;

/**
 * @author Ngoc Huy
 *
 */
public interface DiscriminatorTypeImplementor<T> extends DiscriminatorType<T> {

	DiscriminatorType<T> getDiscriminatorType();

	@Override
	default T stringToObject(String xml) throws Exception {
		// TODO Auto-generated method stub
		return getDiscriminatorType().stringToObject(xml);
	}

	@Override
	default String objectToSQLString(T value, Dialect dialect) throws Exception {
		// TODO Auto-generated method stub
		return getDiscriminatorType().objectToSQLString(value, dialect);
	}

}
