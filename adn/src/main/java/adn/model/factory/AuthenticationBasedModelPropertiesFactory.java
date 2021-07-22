/**
 * 
 */
package adn.model.factory;

import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Map;

import adn.model.DomainEntity;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public interface AuthenticationBasedModelPropertiesFactory {

	<T extends DomainEntity> Map<String, Object> produce(Class<T> type, Object[] properties, String[] columns);

	<T extends DomainEntity> Map<String, Object> produce(Class<T> type, Object[] properties, String[] columns,
			Role role);

	<T extends DomainEntity> List<Map<String, Object>> produce(Class<T> type, List<Object[]> properties,
			String[] columns);

	<T extends DomainEntity> List<Map<String, Object>> produce(Class<T> type, List<Object[]> properties,
			String[] columns, Role role);

	<T extends DomainEntity> String[] validateAndTranslateColumnNames(Class<T> type, Role role,
			String[] requestedColumnNames) throws SQLSyntaxErrorException;

}
