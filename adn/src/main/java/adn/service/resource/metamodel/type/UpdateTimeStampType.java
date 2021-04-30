/**
 * 
 */
package adn.service.resource.metamodel.type;

import static adn.helpers.FunctionHelper.reject;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import org.hibernate.HibernateException;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.TimestampType;
import org.hibernate.type.VersionType;

import adn.service.resource.storage.LocalResourceStorage.ResourceResultSet;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class UpdateTimeStampType extends AbstractTimestampType implements VersionType<Date> {

	public static final UpdateTimeStampType INSTANCE = new UpdateTimeStampType(TimestampType.INSTANCE);

	private final Map<Class<?>, Function<Object, Date>> hydrateFunctions = Map.of(File.class, this::fromFile);

	private final String[] regKeys = new String[] { UpdateTimestamp.class.getName() };

	private UpdateTimeStampType(TimestampType basicType) {
		super(basicType);
	}

	@Override
	public Object hydrate(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		ResourceResultSet resultSet = assertResultSet(rs);
		Object row = getCurrentRow(rs);

		return hydrateFunctions.containsKey(resultSet.getResourceType())
				? hydrateFunctions.get(resultSet.getResourceType()).apply(row)
				: reject(new HibernateException(
						"Unable to hydrate UpdateTimeStamp due to resource type is not supported: "
								+ resultSet.getResourceType()),
						HibernateException.class);
	}

	private Date fromFile(Object o) {
		return new Date(((File) o).lastModified());
	}

	@Override
	public String[] getRegistrationKeys() {
		return regKeys;
	}

	@Override
	public Date seed(SharedSessionContractImplementor session) {
		return ((TimestampType) basicType).seed(session);
	}

	@Override
	public Date next(Date current, SharedSessionContractImplementor session) {
		return ((TimestampType) basicType).next(current, session);
	}

	@Override
	public Comparator<Date> getComparator() {
		return ((TimestampType) basicType).getComparator();
	}

}
