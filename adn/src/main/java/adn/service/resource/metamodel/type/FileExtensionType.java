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

import adn.service.resource.metamodel.Extension;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class FileExtensionType extends AbstractSynthesizedBasicType implements DiscriminatorTypeImplementor<String> {

	public static final FileExtensionType INSTANCE = new FileExtensionType(StringType.INSTANCE);

	private final String[] regKeys = new String[] { Extension.class.getName() };

	private FileExtensionType(StringType basicType) {
		super(basicType);
	}

	@Override
	public Object hydrate(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		// TODO Auto-generated method stub
		return FilenameUtils.getExtension(rs.getString(assertPersister(owner).getIdentifierPropertyName()));
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
