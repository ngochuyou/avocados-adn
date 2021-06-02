/**
 * 
 */
package adn.service.resource.engine.template;

import java.io.File;
import java.sql.ResultSetMetaData;

import org.hibernate.internal.util.MarkerObject;
import org.hibernate.tuple.Tuplizer;

import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.service.resource.engine.tuple.InstantiatorFactory.PojoInstantiator;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceTemplate {

	static final MarkerObject NO_CONTENT = new MarkerObject("<null_content>");

	String getName();

	String getPathColumnName();

	String[] getColumnNames();

	Class<?>[] getColumnTypes();

	default String getDirectoryName() {
		return "";
	}

	PojoInstantiator<File> getInstantiator();

	PropertyAccessImplementor[] getPropertyAccessors();

	Tuplizer getTuplizer();

	ResultSetMetaData getResultSetMetaData();

}
