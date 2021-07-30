/**
 * 
 */
package adn.model.factory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import adn.model.DomainEntity;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public interface AuthenticationBasedModelPropertiesFactory {

	<T extends DomainEntity> Map<String, Object> produce(Class<T> type, Object[] source, String[] columns, Role role);

	<T extends DomainEntity> List<Map<String, Object>> produce(Class<T> type, List<Object[]> sources, String[] columns,
			Role role);

	<T extends DomainEntity> Map<String, Object> singularProduce(Class<T> type, Object source, String column,
			Role role);

	<T extends DomainEntity> List<Map<String, Object>> singularProduce(Class<T> type, List<Object> sources, String column,
			Role role);

	<T extends DomainEntity> Collection<String> validateAndTranslateColumnNames(Class<T> type, Role role,
			Collection<String> requestedColumnNames) throws NoSuchFieldException;

}
