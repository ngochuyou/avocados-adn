/**
 * 
 */
package adn.service.resource.type;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.AbstractStandardBasicType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.descriptor.JdbcTypeNameMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.service.resource.factory.DefaultResourceIdentifierGenerator.ResourceIdentifierPart;
import adn.service.resource.model.models.Resource;
import adn.service.resource.type.AbstractSyntheticBasicType.AbstractFieldBasedSyntheticBasicType;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class FileCreationTimeStampType extends AbstractFieldBasedSyntheticBasicType {

	public static final String NAME = "adn.service.resource.metamodel.type.FileCreationTimeStampType";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String[] regKeys = new String[] { NAME };

	public FileCreationTimeStampType() {
		this(Resource.RESOURCE_IDENTIFIER_ATTRIBUTE_NAME);
	}

	public FileCreationTimeStampType(String referencedFieldName) {
		super(TimestampType.INSTANCE, referencedFieldName);
	}

	@Override
	public Object hydrate(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		Date stamp = ResourceIdentifierPart.getCreationTimeStamp(rs.getString(referencedFieldName));

		logger.debug(String.format("extracted value ([%s] : [%s]) - [%s]", names[0],
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
