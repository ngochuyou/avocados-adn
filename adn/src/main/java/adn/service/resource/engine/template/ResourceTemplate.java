/**
 * 
 */
package adn.service.resource.engine.template;

import java.io.File;
import java.sql.ResultSetMetaData;

import org.hibernate.tuple.Tuplizer;

import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.service.resource.engine.tuple.InstantiatorFactory.PojoInstantiator;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceTemplate {

	String getName();

	String getPathColumnName();

	String[] getColumnNames();

	Class<?>[] getColumnTypes();

	PojoInstantiator<File> getInstantiator();

	PropertyAccessImplementor[] getPropertyAccessors();

	Tuplizer getTuplizer();

	ResultSetMetaData getResultSetMetaData();

}
