/**
 * 
 */
package adn.model.factory;

import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import adn.model.DepartmentScoped;

/**
 * @author Ngoc Huy
 *
 */
public interface DepartmentBasedModelPropertiesFactory {

	<T extends DepartmentScoped> Map<String, Object> produce(Class<T> type, Object[] source, String[] columns,
			UUID departmentId);

	<T extends DepartmentScoped> List<Map<String, Object>> produce(Class<T> type, List<Object[]> sources,
			String[] columns, UUID departmentId);

	<T extends DepartmentScoped> String[] validateColumnNames(Class<T> type, String[] requestedColumns)
			throws SQLSyntaxErrorException;

}
