/**
 * 
 */
package adn.service.resource.type;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hibernate.HibernateException;
import org.hibernate.type.BinaryType;
import org.hibernate.type.descriptor.java.PrimitiveByteArrayTypeDescriptor;

import adn.helpers.FunctionHelper.HandledFunction;
import adn.service.resource.engine.access.PropertyAccess;
import adn.service.resource.engine.access.PropertyAccess.Type;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class FileContentByByteArrayType extends AbstractExplicitlyBindedType<byte[]>
		implements NoOperationSet, HandledFunction<File, byte[], RuntimeException> {

	public static final String NAME = "adn.service.resource.metamodel.type.ExplicitlyHydratedFileContextType";
	public static final int MAX_SIZE_IN_ONE_READ = 5 * 1024 * 1024; // 5MB

	public FileContentByByteArrayType() {
		super(BinaryType.INSTANCE.getSqlTypeDescriptor(), OptimalLogPrimitiveByteArrayTypeDescriptor.INSTANCE);
	}

	@Override
	@PropertyAccess(type = Type.GETTER, clazz = HandledFunction.class)
	public byte[] apply(File arg) throws HibernateException {
		try {
			Path path = ((File) arg).toPath();

			if (Files.size(path) > MAX_SIZE_IN_ONE_READ) {
				throw new HibernateException(
						String.format("File size is too large to read into byte[], max size in one read is [%s] MB",
								MAX_SIZE_IN_ONE_READ));
			}

			return Files.readAllBytes(path);
		} catch (Exception e) {
			throw new HibernateException(e);
		}
	}

	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * The
	 * {@link PrimitiveByteArrayTypeDescriptor#extractLoggableRepresentation(byte[])}
	 * takes too long trying to log the whole byte[], this subclass is to override
	 * that method so that we log the byte[].length instead
	 * 
	 * @author Ngoc Huy
	 *
	 */
	public static class OptimalLogPrimitiveByteArrayTypeDescriptor extends PrimitiveByteArrayTypeDescriptor {

		public static final OptimalLogPrimitiveByteArrayTypeDescriptor INSTANCE = new OptimalLogPrimitiveByteArrayTypeDescriptor();

		private OptimalLogPrimitiveByteArrayTypeDescriptor() {}

		@Override
		public String extractLoggableRepresentation(byte[] value) {
			return String.format("<byte_array:[%d]>", value.length);
		}

	}

}
