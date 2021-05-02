/**
 * 
 */
package adn.service.resource.metamodel.type;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.io.FilenameUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.service.resource.metamodel.Extension;
import adn.service.resource.metamodel.type.AbstractSyntheticBasicType.AbstractFieldBasedSyntheticBasicType;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class FileExtensionType extends AbstractFieldBasedSyntheticBasicType
		implements DiscriminatorTypeImplementor<String> {

	public static final FileExtensionType INSTANCE = new FileExtensionType(StringType.INSTANCE, "name");

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final String[] regKeys = new String[] { Extension.class.getName() };

	private FileExtensionType(StringType basicType, String referencedFieldName) {
		super(basicType, referencedFieldName);
	}

	@Override
	public Object hydrate(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		String value = "." + FilenameUtils.getExtension(rs.getString(referencedFieldName));

		logger.debug(String.format("Successfully extracted [%s] with value [%s]", referencedFieldName, value));

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
