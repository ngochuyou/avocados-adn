/**
 * 
 */
package adn.service.resource.metamodel.type;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.java.PrimitiveByteArrayTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class ByteArrayType extends AbstractSingleColumnStandardBasicType<byte[]> {

	public static final ByteArrayType INSTANCE = new ByteArrayType();

	private ByteArrayType() {
		super(VarcharTypeDescriptor.INSTANCE, PrimitiveByteArrayTypeDescriptor.INSTANCE);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "bytes";
	}

}
