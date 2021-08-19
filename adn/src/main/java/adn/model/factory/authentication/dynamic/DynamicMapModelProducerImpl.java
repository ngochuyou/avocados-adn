/**
 * 
 */
package adn.model.factory.authentication.dynamic;

import static adn.helpers.LoggerHelper.with;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.application.context.builders.CredentialFactory;
import adn.helpers.StringHelper;
import adn.helpers.TypeHelper;
import adn.model.DomainEntity;
import adn.model.entities.metadata.DomainEntityMetadata;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.DynamicMapModelProducerFactory;
import adn.model.factory.authentication.SecuredProperty;

/**
 * @author Ngoc Huy
 *
 */
public class DynamicMapModelProducerImpl<T extends DomainEntity> implements DynamicMapModelProducer<T> {

	private final String[] properties;

	private final Map<String, Map<String, BiFunction<Object, Credential, Object>>> producingFunctions;
	private final Map<String, Map<String, String>> aliasMap;
	private final Map<String, Map<String, String>> originalNamesMap;

	/**
	 * 
	 */
	public DynamicMapModelProducerImpl(Class<T> entityType, Set<SecuredProperty<T>> properties,
			DomainEntityMetadata metadata, CredentialFactory credentialFactory) {
		Logger logger = LoggerFactory.getLogger(this.getClass());
		Map<String, Map<String, BiFunction<Object, Credential, Object>>> producingFunctions = new HashMap<>(0);
		Map<String, Map<String, String>> aliasMap = new HashMap<>(0);
		Map<String, Map<String, String>> originalNamesMap = new HashMap<>(0);

		this.properties = metadata.getPropertyNames().toArray(String[]::new);
		// @formatter:off
		properties.stream().filter(prop -> {
			boolean isParent = TypeHelper.isParentOf(prop.getOwningType(), entityType);
			boolean unknownProperty = !metadata.hasProperty(prop.getName());
			boolean nullFunction = prop.getFunction() == null;
			
			if (!isParent || unknownProperty || nullFunction) {
				logger.trace(String.format("[%s]: Ignoring %s [%s] %s", entityType.getName(),
						SecuredProperty.class.getSimpleName(), prop.getName(),
						!isParent ? "invalid type"
								: unknownProperty ? "unknown property"
										: " null function"));
				return false;
			}

			return true;
		})
		.sorted((o, t) -> o.getOwningType().equals(t.getOwningType()) ? 0
				: (TypeHelper.isExtendedFrom(o.getOwningType(), t.getOwningType()) ? 1 : -1))
		.forEach(prop -> {
			String name = prop.getName();
			String alias = prop.getAlias();
			BiFunction<Object, Credential, Object> function = prop.getFunction();
			String credential = prop.getCredential().evaluate();
			
			if (!producingFunctions.containsKey(credential)) {
				producingFunctions.put(credential,
						Stream.of(Map.entry(name, function))
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
				aliasMap.put(credential,
						Stream.of(Map.entry(name, StringHelper.hasLength(alias) ? with(logger).debug(String.format("Using alternative name [%s] on property [%s]", alias, name), alias) : name))
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
				return;
			}
			
			Map<String, BiFunction<Object, Credential, Object>> functions = producingFunctions.get(credential);
			Map<String, String> aliases = aliasMap.get(credential);
			
			if (functions.containsKey(name)) {
				if (functions.get(name) != function) {
					BiFunction<Object, Credential, Object> oldFnc = functions.put(name, function);
					
					logger.debug(String.format("[%s#%s] Overriding function of property [%s] using [%s], overridden function was [%s]",
							entityType, credential, name, function, oldFnc));
				}

				if (!aliases.get(name).equals(alias)) {
					String oldAlias = aliases.put(name, alias);
					
					logger.debug(String.format("Overriding alternative name using [%s], overridden name was [%s]",
							alias, oldAlias));
				}
				return;
			}
			
			functions.put(name, function);
			aliases.put(name,
					StringHelper.hasLength(alias) ?
							with(logger).debug(String.format("Using alternative name [%s] on property [%s]", alias, name), alias) :
							name);
		});;
		// @formatter:on
		producingFunctions.entrySet().forEach(entry -> {
			metadata.getPropertyNames().stream().forEach(propName -> {
				entry.getValue().computeIfAbsent(propName, key -> with(logger).trace(
						String.format("[%s#%s]: Masking property [%s]", entityType.getName(), entry.getKey(), propName),
						MASKER));
			});
		});
		aliasMap.values().forEach(propNames -> {
			metadata.getPropertyNames().stream().forEach(propName -> {
				propNames.computeIfAbsent(propName, key -> propName);
//				propNames.merge(propName, propName, (o, n) -> Optional.ofNullable(o).orElse(n));
			});
		});
		originalNamesMap = aliasMap.entrySet().stream()
				.map(entry -> Map.entry(entry.getKey(),
						entry.getValue().entrySet().stream()
								.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey))))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		credentialFactory.getCredentials().stream().forEach(credential -> {
			String credentialString = credential.evaluate();

			if (producingFunctions.containsKey(credentialString)) {
				return;
			}

			logger.debug(String.format("Masking every properties against Credential [%s]", credentialString));

			producingFunctions.put(credentialString,
					metadata.getPropertyNames().stream().map(propName -> Map.entry(propName, MASKER))
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
		});

		this.producingFunctions = producingFunctions;
		this.aliasMap = Collections.unmodifiableMap(aliasMap);
		this.originalNamesMap = Collections.unmodifiableMap(originalNamesMap);
	}

	@Override
	public Map<String, Object> produce(Object[] source, Credential credential) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> produce(List<Object[]> source, Credential credential) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> produce(Object[] source, Credential credential, String[] columns) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> produce(List<Object[]> source, Credential credential, String[] columns) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> produce(Object source, Credential credential, String column) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Map<String, Object>> produce(List<Object> source, Credential credential, String columns) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> validateColumns(Credential credential, Collection<String> columns) throws NoSuchFieldException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void afterFactoryBuild(DynamicMapModelProducerFactory factory) {

	}

}
