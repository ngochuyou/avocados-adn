/**
 * 
 */
package adn.service.resource.type;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.BasicType;

import adn.helpers.FunctionHelper.HandledFunction;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class ExplicitlyHydratedType<T, E extends HibernateException> extends AbstractSyntheticBasicType {

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
		return function.apply(Optional.ofNullable(assertResultSet(rs).getCurrentRow())
				.orElseThrow(() -> new HibernateException("NULL returned by ResultSet")));
	}

	public HandledFunction<Object, T, E> getFunction() {
		return function;
	}

	public Class<T> getReturnedType() {
		return returnedType;
	}

	@Override
	public boolean isAssociationType() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isComponentType() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCollectionType() {
		// TODO Auto-generated method stub
		return false;
	}

}