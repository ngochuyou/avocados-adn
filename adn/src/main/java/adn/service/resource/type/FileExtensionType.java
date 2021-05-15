/**
 * 
 */
package adn.service.resource.type;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.StringType;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class FileExtensionType extends AbstractExplicitlyExtractedType<File, String>
		implements DiscriminatorType<String>, NoOperationSet {

	public static final String NAME = "adn.service.resource.metamodel.type.FileExtensionType";

	private final String[] regKeys = new String[] { NAME };

	public FileExtensionType() {
		super(StringType.INSTANCE.getSqlTypeDescriptor(), StringType.INSTANCE.getJavaTypeDescriptor());
	}

	@Override
	public String[] getRegistrationKeys() {
		return regKeys;
	}

	@Override
	public String stringToObject(String xml) throws Exception {
		return null;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String objectToSQLString(String value, Dialect dialect) throws Exception {
		// TODO Auto-generated method stub
		return value;
	}

	@Override
	public String apply(File file) {
		return "." + FilenameUtils.getExtension(file.getName());
	}

}
