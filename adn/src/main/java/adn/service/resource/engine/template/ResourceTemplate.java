/**
 * 
 */
package adn.service.resource.engine.template;

import org.hibernate.internal.util.MarkerObject;

import adn.service.resource.engine.Storage;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.service.resource.engine.tuple.ResourceTuplizer;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceTemplate {

	static final MarkerObject NO_CONTENT = new MarkerObject("<null_content>");

	String getTemplateName();

	String getDirectory();

	String getPathColumn();

	Class<?> getPathType();

	String getExtensionColumn();

	Class<?> getExtensionType();

	String getContentColumn();

	Class<?> getContentType();

	int getPropertySpan();

	String[] getColumnNames();

	Class<?>[] getColumnTypes();

	Integer getColumnIndex(String columnName);

	PropertyAccessImplementor[] getPropertyAccessors();

	PropertyAccessImplementor getPropertyAccess(Integer i);

	PropertyAccessImplementor getPropertyAccess(String columnName);

	ResourceTuplizer getTuplizer();

	Storage getStorage();

}
