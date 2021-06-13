/**
 * 
 */
package adn.service.resource.model.type;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.hibernate.type.BinaryType;
import org.hibernate.type.descriptor.java.PrimitiveByteArrayTypeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.helpers.FunctionHelper.HandledBiFunction;
import adn.service.resource.engine.access.PropertyAccess;
import adn.service.resource.engine.access.PropertyAccess.Type;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class FileContentByByteArrayType extends AbstractExplicitlyBindedType<byte[]>
		implements HandledBiFunction<File, byte[], File, RuntimeException> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String NAME = "adn.service.resource.metamodel.type.ExplicitlyHydratedFileContextType";
	public static final int MAX_SIZE_IN_ONE_READ = 5 * 1024 * 1024; // 5MB

	public FileContentByByteArrayType() {
		super(BinaryType.INSTANCE.getSqlTypeDescriptor(), OptimalLogPrimitiveByteArrayTypeDescriptor.INSTANCE);
	}

	@Override
	@PropertyAccess(type = Type.SETTER, clazz = HandledBiFunction.class)
	public File apply(File file, byte[] content) throws RuntimeException {
		try {
			if (isSame(file, content)) {
				if (logger.isTraceEnabled()) {
					logger.trace("Skip writing file since the original content and requested content are the same");
				}

				return file;
			}

			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Writing file [%s], content length [%s]", file.getPath(), content.length));
			}

			Files.write(Paths.get(file.getPath()), content);

			return file;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	private boolean isSame(File file, byte[] content) throws IOException {
		if (!file.exists() || content == null) {
			return false;
		}

		BufferedInputStream originalBuffer = new BufferedInputStream(new FileInputStream(file));
		BufferedInputStream requestedBuffer;

		try {
			requestedBuffer = new BufferedInputStream(new ByteArrayInputStream(content));
		} catch (RuntimeException ioe) {
			originalBuffer.close();
			throw ioe;
		}

		try {
			int chunkSize = 8192;
			byte[] originalChunk = new byte[chunkSize];
			byte[] requestedChunk = new byte[chunkSize];
			int offset = 0;

			while (originalBuffer.read(originalChunk, offset, chunkSize) != -1
					&& requestedBuffer.read(requestedChunk, offset, chunkSize) != -1) {
				if (Arrays.compare(originalChunk, requestedChunk) != 0) {
					return false;
				}

				if (originalBuffer.read() == -1 || requestedBuffer.read() == -1) {
					return true;
				}

				chunkSize *= 2;
				originalChunk = new byte[chunkSize];
				requestedChunk = new byte[chunkSize];
			}

			return false;
		} finally {
			originalBuffer.close();
			requestedBuffer.close();
		}
	}

	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * The
	 * {@link PrimitiveByteArrayTypeDescriptor#extractLoggableRepresentation(byte[])}
	 * takes too long trying to log the whole byte[], gives a blank output
	 * eventually, this subclass is to override that method so that we log the
	 * byte[].length instead
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
