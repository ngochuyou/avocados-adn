/**
 * 
 */
package adn.model.factory.authentication.dynamic;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import adn.model.DomainEntity;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.ModelProducer;
import adn.model.factory.authentication.DynamicMapModelProducerFactory;

/**
 * @author Ngoc Huy
 *
 */
public interface DynamicMapModelProducer<T extends DomainEntity> extends ModelProducer<Object[], Map<String, Object>> {

	Map<String, Object> produce(Object[] source, Credential credential, String[] columns);

	List<Map<String, Object>> produce(List<Object[]> source, Credential credential, String[] columns);

	Map<String, Object> produce(Object source, Credential credential, String column);

	List<Map<String, Object>> produce(List<Object> source, Credential credential, String columns);

	List<String> validateColumns(Credential credential, Collection<String> columns) throws NoSuchFieldException;

	void afterFactoryBuild(DynamicMapModelProducerFactory factory);

}
