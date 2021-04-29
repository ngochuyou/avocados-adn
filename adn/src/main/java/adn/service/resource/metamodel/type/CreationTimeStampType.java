/**
 * 
 */
package adn.service.resource.metamodel.type;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.TimestampType;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class CreationTimeStampType extends AbstractTimestampType {

	public static final CreationTimeStampType INSTANCE = new CreationTimeStampType(TimestampType.INSTANCE);

	private CreationTimeStampType(TimestampType basicType) {
		super(basicType);
	}

	@Override
	public Object hydrate(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getRegistrationKeys() {
		// TODO Auto-generated method stub
		return new String[] { CreationTimestamp.class.getName() };
	}

}
