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
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.LiteralType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.VersionType;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class UpdateTimestampType extends AbstractTimestampType implements VersionType<Date>, LiteralType<Date> {

	public static final UpdateTimestampType INSTANCE = new UpdateTimestampType(TimestampType.INSTANCE);

	private final String[] regKeys = new String[] { UpdateTimestamp.class.getName() };

	private UpdateTimestampType(TimestampType basicType) {
		super(basicType);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object hydrate(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		// TODO Auto-generated method stub
		return basicType.hydrate(rs, names, session, owner);
	}

	@Override
	public String[] getRegistrationKeys() {
		// TODO Auto-generated method stub
		return regKeys;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String objectToSQLString(Date value, Dialect dialect) throws Exception {
		// TODO Auto-generated method stub
		return ((LiteralType<Date>) basicType).objectToSQLString(value, dialect);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Date seed(SharedSessionContractImplementor session) {
		// TODO Auto-generated method stub
		return ((VersionType<Date>) basicType).seed(session);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Date next(Date current, SharedSessionContractImplementor session) {
		// TODO Auto-generated method stub
		return ((VersionType<Date>) basicType).next(current, session);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Comparator<Date> getComparator() {
		// TODO Auto-generated method stub
		return ((VersionType<Date>) basicType).getComparator();
	}

}
