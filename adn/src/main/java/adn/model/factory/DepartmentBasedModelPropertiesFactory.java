/**
 * 
 */
package adn.model.factory;

import java.util.Collection;
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

	<T extends DepartmentScoped> Map<String, Object> singularProduce(Class<T> type, Object source, String column,
			UUID departmentId);

	<T extends DepartmentScoped> List<Map<String, Object>> singularProduce(Class<T> type, List<Object> sources,
			String column, UUID departmentId);

	<T extends DepartmentScoped> Collection<String> validateColumnNames(Class<T> type,
			Collection<String> requestedColumns) throws NoSuchFieldException;

}
