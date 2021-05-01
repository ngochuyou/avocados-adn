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

import adn.service.resource.metamodel.DefaultResourceIdentifierGenerator.ResourceIdentifierPart;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class CreationTimeStampType extends AbstractSyntheticBasicType {

	public static final CreationTimeStampType INSTANCE = new CreationTimeStampType(TimestampType.INSTANCE);

	private final String[] regKeys = new String[] { CreationTimestamp.class.getName() };

	private CreationTimeStampType(TimestampType basicType) {
		super(basicType);
	}

	@Override
	public Object hydrate(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		return ResourceIdentifierPart
				.getCreationTimeStamp(rs.getString(assertPersister(owner).getIdentifierPropertyName()));
	}

	@Override
	public String[] getRegistrationKeys() {
		return regKeys;
	}

}
