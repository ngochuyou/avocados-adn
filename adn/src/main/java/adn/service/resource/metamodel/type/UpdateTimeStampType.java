/**
 * 
 */
package adn.service.resource.metamodel.type;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.TimestampType;
import org.hibernate.type.VersionType;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class UpdateTimeStampType extends AbstractTimestampType implements VersionType<Date> {

	public static final UpdateTimeStampType INSTANCE = new UpdateTimeStampType(TimestampType.INSTANCE);

	private UpdateTimeStampType(TimestampType basicType) {
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
		return new String[] { UpdateTimestamp.class.getName() };
	}

	@Override
	public Date seed(SharedSessionContractImplementor session) {
		// TODO Auto-generated method stub
		return ((TimestampType) basicType).seed(session);
	}

	@Override
	public Date next(Date current, SharedSessionContractImplementor session) {
		// TODO Auto-generated method stub
		return ((TimestampType) basicType).next(current, session);
	}

	@Override
	public Comparator<Date> getComparator() {
		// TODO Auto-generated method stub
		return ((TimestampType) basicType).getComparator();
	}

}
