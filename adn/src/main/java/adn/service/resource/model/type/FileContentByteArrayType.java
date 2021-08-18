/**
 * 
 */
package adn.service.resource.model.type;

import org.hibernate.type.BinaryType;
import org.hibernate.type.descriptor.java.PrimitiveByteArrayTypeDescriptor;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class FileContentByteArrayType extends AbstractExplicitlyBindedType<byte[]> {

	public static final String NAME = "adn.service.resource.metamodel.type.FileContentByteArrayType";

	public FileContentByteArrayType() {
		super(BinaryType.INSTANCE.getSqlTypeDescriptor(), OptimalLogPrimitiveByteArrayTypeDescriptor.INSTANCE);
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
