/**
 * 
 */
package adn.service.resource.type;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

import adn.service.resource.engine.access.Extractor;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractExplicitlyExtractedType<T, R> extends AbstractSingleColumnStandardBasicType<R>
		implements Extractor<T, R> {

	public AbstractExplicitlyExtractedType(SqlTypeDescriptor sqlTypeDescriptor, JavaTypeDescriptor<R> javaTypeDescriptor) {
		super(sqlTypeDescriptor, javaTypeDescriptor);
	}

}
