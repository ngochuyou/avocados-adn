/**
 * 
 */
package adn.service.resource.type;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractExplicitlyExtractedType<T, R, E extends RuntimeException>
		extends AbstractSingleColumnStandardBasicType<R> {

	public AbstractExplicitlyExtractedType(SqlTypeDescriptor sqlTypeDescriptor,
			JavaTypeDescriptor<R> javaTypeDescriptor) {
		super(sqlTypeDescriptor, javaTypeDescriptor);
	}

}
