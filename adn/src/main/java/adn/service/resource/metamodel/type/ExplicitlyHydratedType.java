/**
 * 
 */
package adn.service.resource.metamodel.type;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.BasicType;

import adn.helpers.FunctionHelper.HandledFunction;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class ExplicitlyHydratedType<T, E extends HibernateException> extends AbstractTranslatedBasicType {

	private final String[] regKeys = new String[] { };

	private final Class<T> returnedType;
	private final HandledFunction<Object, T, E> function;

	public ExplicitlyHydratedType(BasicType basicType, Class<T> returnedType, HandledFunction<Object, T, E> function) {
		super(basicType);
		this.returnedType = returnedType;
		this.function = function;
	}

	@Override
	public String[] getRegistrationKeys() {
		// TODO Auto-generated method stub
		return regKeys;
	}

	@Override
	public Object hydrate(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		// TODO Auto-generated method stub
		return function.apply(getCurrentRow(rs));
	}

	public HandledFunction<Object, T, E> getFunction() {
		return function;
	}

	public Class<T> getReturnedType() {
		return returnedType;
	}

}
