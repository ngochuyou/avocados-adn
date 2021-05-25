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
public abstract class AbstractExplicitlyBindedType<R> extends AbstractSingleColumnStandardBasicType<R> {

	public AbstractExplicitlyBindedType(SqlTypeDescriptor sqlTypeDescriptor,
			JavaTypeDescriptor<R> javaTypeDescriptor) {
		super(sqlTypeDescriptor, javaTypeDescriptor);
	}

}
