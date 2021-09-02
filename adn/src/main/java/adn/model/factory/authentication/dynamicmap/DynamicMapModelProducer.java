/**
 * 
 */
package adn.model.factory.authentication.dynamicmap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import adn.model.DomainEntity;
import adn.model.factory.authentication.BatchedPojoSource;
import adn.model.factory.authentication.BatchedSource;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.DynamicMapModelProducerFactory;
import adn.model.factory.authentication.ModelProducer;
import adn.model.factory.authentication.SinglePojoSource;
import adn.model.factory.authentication.SingleSource;

/**
 * @author Ngoc Huy
 *
 */
public interface DynamicMapModelProducer<T extends DomainEntity> extends ModelProducer<Object[], Map<String, Object>> {

	<E extends T> Map<String, Object> produceSingleSource(SingleSource<E> source, Credential credential)
			throws UnauthorizedCredential;

	<E extends T> List<Map<String, Object>> produceBatchedSource(BatchedSource<E> sourceBatch, Credential credential)
			throws UnauthorizedCredential;

	<E extends T> Map<String, Object> produceSinglePojo(SinglePojoSource<E> source, Credential credential)
			throws UnauthorizedCredential;

	<E extends T> List<Map<String, Object>> produceBatchedPojo(BatchedPojoSource<E> sourceBatch, Credential credential)
			throws UnauthorizedCredential;

	List<String> validateColumns(Credential credential, Collection<String> columns) throws NoSuchFieldException, UnauthorizedCredential;

	void afterFactoryBuild(DynamicMapModelProducerFactory factory);

}
