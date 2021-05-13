/**
 * 
 */
package adn.service.resource.type;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.io.FilenameUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.AbstractStandardBasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.StringType;
import org.hibernate.type.descriptor.JdbcTypeNameMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.service.resource.model.models.Resource;
import adn.service.resource.type.AbstractSyntheticBasicType.AbstractFieldBasedSyntheticBasicType;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class FileExtensionType extends AbstractFieldBasedSyntheticBasicType
		implements DiscriminatorTypeImplementor<String> {

	public static final String NAME = "adn.service.resource.metamodel.type.FileExtensionType";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String[] regKeys = new String[] { NAME };

	public FileExtensionType() {
		this(Resource.RESOURCE_IDENTIFIER_ATTRIBUTE_NAME);
	}

	public FileExtensionType(String referencedFieldName) {
		super(StringType.INSTANCE, referencedFieldName);
	}

	@Override
	public Object hydrate(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		String value = "." + FilenameUtils.getExtension(rs.getString(referencedFieldName));

		logger.debug(String.format("extracted value ([%s] : [%s]) - [%s]", names[0],
				basicType instanceof AbstractStandardBasicType
						? JdbcTypeNameMapper.getTypeName(
								((AbstractStandardBasicType<?>) basicType).getSqlTypeDescriptor().getSqlType())
						: basicType.getName(),
				value.toString()));

		return value;
	}

	@Override
	public String[] getRegistrationKeys() {
		// TODO Auto-generated method stub
		return regKeys;
	}

	@SuppressWarnings("unchecked")
	@Override
	public DiscriminatorType<String> getDiscriminatorType() {
		// TODO Auto-generated method stub
		return (DiscriminatorType<String>) basicType;
	}

}
