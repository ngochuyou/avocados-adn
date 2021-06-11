/**
 * 
 */
package adn.service.resource.type;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.helpers.FunctionHelper.HandledBiFunction;
import adn.helpers.FunctionHelper.HandledFunction;
import adn.service.resource.engine.access.PropertyAccess;
import adn.service.resource.engine.access.PropertyAccess.Type;

/**
 * 
 * 
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class FileExtensionType extends AbstractExplicitlyBindedType<String> implements DiscriminatorType<String>,
		HandledFunction<File, String, RuntimeException>, HandledBiFunction<File, String, File, RuntimeException> {

	public static final String NAME = "adn.service.resource.metamodel.type.FileExtensionType";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
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
		return value;
	}

	@Override
	@PropertyAccess(type = Type.GETTER, clazz = HandledFunction.class)
	public String apply(File file) {
		return "." + FilenameUtils.getExtension(file.getName());
	}

	@Override
	@PropertyAccess(type = Type.SETTER, clazz = HandledBiFunction.class)
	public File apply(File file, String extension) throws RuntimeException {
		extension = extension.startsWith(".") ? extension : "." + extension;

		if (file.getPath().endsWith(extension)) {
			return file;
		}

		Path source = Paths.get(file.getPath());
		String directory = file.getParent();
		String newName = String.format("%s%s", file.getName().replaceFirst("\\.[\\d\\w]+$", ""), extension);

		if (Files.exists(Paths.get(newName))) {
			throw new RuntimeException(
					String.format("Unable to rename file [%s] -> [%s] since new name already existed",
							source.toAbsolutePath(), newName));
		}

		try {
			logger.trace(String.format("Renaming file [%s] -> [%s]", source.toAbsolutePath(), newName));

			Files.move(source, source.resolveSibling(newName));

			return new File(String.format("%s\\%s", directory, newName));
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

}
