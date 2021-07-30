/**
 * 
 */
package adn.model.factory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import adn.model.factory.property.production.ModelPropertiesProducer;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public interface AuthenticationBasedModelPropertiesProducer
		extends ModelPropertiesProducer, AuthenticationBasedModelProducer<Object[], Map<String, Object>> {

	@Override
	default Map<String, Object> produce(Object[] source) {
		return produce(source, null);
	}

	@Override
	default List<Map<String, Object>> produce(List<Object[]> source) {
		return produce(source, null);
	}

	Map<String, Object> produce(Object[] source, Role role);

	List<Map<String, Object>> produce(List<Object[]> source, Role role);

	Map<String, Object> produce(Object[] source, Role role, String[] columnNames);

	List<Map<String, Object>> produce(List<Object[]> source, Role role, String[] columnNames);

	default Map<String, Object> singularProduce(Object source, String columnName) {
		return singularProduce(source, null, columnName);
	}

	default List<Map<String, Object>> singularProduce(List<Object> source, String columnName) {
		return singularProduce(source, null, columnName);
	}

	Map<String, Object> singularProduce(Object source, Role role, String columnName);

	List<Map<String, Object>> singularProduce(List<Object> source, Role role, String columnName);

	@Override
	default Collection<String> validateAndTranslateColumnNames(Collection<String> requestedColumns)
			throws NoSuchFieldException {
		return validateAndTranslateColumnNames(null, requestedColumns);
	}

	Collection<String> validateAndTranslateColumnNames(Role role, Collection<String> requestedColumns)
			throws NoSuchFieldException;

}
