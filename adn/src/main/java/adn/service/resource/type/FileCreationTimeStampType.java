/**
 * 
 */
package adn.service.resource.type;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.type.DateType;

import adn.service.resource.factory.DefaultResourceIdentifierGenerator.ResourceIdentifierPart;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class FileCreationTimeStampType extends AbstractExplicitlyExtractedType<File, Date> implements NoOperationSet {

	public static final String NAME = "adn.service.resource.metamodel.type.FileCreationTimeStampType";

	private final String[] regKeys = new String[] { NAME };

	public FileCreationTimeStampType() {
		super(DateType.INSTANCE.getSqlTypeDescriptor(), DateType.INSTANCE.getJavaTypeDescriptor());
	}

	@Override
	public String[] getRegistrationKeys() {
		return regKeys;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Date apply(File file) throws HibernateException {
		try {
			return ResourceIdentifierPart.getCreationTimeStamp(file.getName());
		} catch (NumberFormatException | SQLException e) {
			throw new HibernateException(e);
		}
	}

}
