/**
 * 
 */
package adn.service.resource.metamodel.type;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.AbstractStandardBasicType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.descriptor.JdbcTypeNameMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.service.resource.metamodel.DefaultResourceIdentifierGenerator.ResourceIdentifierPart;
import adn.service.resource.metamodel.type.AbstractSyntheticBasicType.AbstractFieldBasedSyntheticBasicType;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class CreationTimeStampType extends AbstractFieldBasedSyntheticBasicType {

	public static final CreationTimeStampType INSTANCE = new CreationTimeStampType(TimestampType.INSTANCE, "name");

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String[] regKeys = new String[] { CreationTimestamp.class.getName() };

	private CreationTimeStampType(TimestampType basicType, String referencedFieldName) {
		super(basicType, referencedFieldName);
	}

	@Override
	public Object hydrate(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		Date stamp = ResourceIdentifierPart.getCreationTimeStamp(rs.getString(referencedFieldName));

		logger.debug(String.format("extracted value ([%s] : [%s]) - [%s]", referencedFieldName,
				basicType instanceof AbstractStandardBasicType
						? JdbcTypeNameMapper.getTypeName(
								((AbstractStandardBasicType<?>) basicType).getSqlTypeDescriptor().getSqlType())
						: basicType.getName(),
				stamp.toString()));

		return stamp;
	}

	@Override
	public String[] getRegistrationKeys() {
		return regKeys;
	}

}
