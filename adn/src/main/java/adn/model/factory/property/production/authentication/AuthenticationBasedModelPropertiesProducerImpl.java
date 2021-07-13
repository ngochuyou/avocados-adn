/**
 * 
 */
package adn.model.factory.property.production.authentication;

import static adn.helpers.LoggerHelper.with;

import java.sql.SQLSyntaxErrorException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.application.context.ContextProvider;
import adn.helpers.StringHelper;
import adn.helpers.TypeHelper;
import adn.helpers.Utils.Entry;
import adn.model.AbstractModel;
import adn.model.ModelContextProvider;
import adn.model.entities.metadata.EntityMetadata;
import adn.model.factory.AuthenticationBasedModelPropertiesProducer;
import adn.model.factory.property.production.SecuredProperty;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public class AuthenticationBasedModelPropertiesProducerImpl implements AuthenticationBasedModelPropertiesProducer {

	private static final float LOAD_FACTOR = 1.175f;

	private final Map<Role, Map<String, Function<Object, Object>>> functionsMap;
	private final Map<Role, Map<String, String>> alternativeNamesByOriginalNames;
	private final Map<Role, Map<String, String>> originalNamesByAlternativeNames;

	private final String[] properties;

	public <T extends AbstractModel> AuthenticationBasedModelPropertiesProducerImpl(Class<T> entityClass,
			Set<SecuredProperty<T>> securedProperties) {
		final Logger logger = LoggerFactory.getLogger(this.getClass());
		EntityMetadata metadata = ContextProvider.getBean(ModelContextProvider.class).getMetadata(entityClass);
		Map<Role, Map<String, Function<Object, Object>>> functionsMap = new HashMap<>(0, 1f);
		Map<Role, Map<String, String>> alternativeNamesMap = new HashMap<>(0, 1f);
		// @formatter:off
		securedProperties.stream().filter(prop -> {
			boolean isParent = TypeHelper.isParentOf(prop.getEntityType(), entityClass);
			boolean unknownProperty = !metadata.hasAttribute(prop.getPropertyName());
			boolean nullFunction = prop.getFunction() == null;
			
			if (!isParent || unknownProperty || nullFunction) {
				logger.info(String.format("[%s]: Ignoring %s [%s] %s", entityClass.getName(),
						SecuredProperty.class.getSimpleName(), prop.getPropertyName(),
						!isParent ? "invalid type"
								: unknownProperty ? "unknown property"
										: " null function"));
				return false;
			}

			return true;
		}).sorted((o, t) -> o.getEntityType().equals(t.getEntityType()) ? 0
				: (TypeHelper.isExtendedFrom(o.getEntityType(), t.getEntityType()) ? 1 : -1)
		).forEach(prop -> {
			String name = prop.getPropertyName();
			String altName = prop.getPropertyAlternativeName();
			Function<Object, Object> fnc = prop.getFunction();
			
			if (!functionsMap.containsKey(prop.getRole())) {
				functionsMap.put(prop.getRole(),
						Stream.of(Map.entry(name, fnc))
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
				alternativeNamesMap.put(prop.getRole(),
						Stream.of(Map.entry(name, StringHelper.hasLength(altName) ? with(logger).trace(String.format("Using alternative name [%s] on property [%s]", altName, name), altName) : name))
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
				return;
			}

			Map<String, Function<Object, Object>> functions = functionsMap.get(prop.getRole());
			Map<String, String> alternativeNames = alternativeNamesMap.get(prop.getRole());

			if (functions.containsKey(name)) {
				if (functions.get(name) != fnc) {
					Function<Object, Object> oldFnc = functions.put(name, fnc);
					
					logger.debug(String.format("[%s#%s] Overriding function of property [%s] using [%s], overridden function was [%s]",
							entityClass, prop.getRole(), name, fnc, oldFnc));
				}

				if (altName != null && !alternativeNames.get(name).equals(altName)) {
					String oldAltName = alternativeNames.put(name, altName);
					
					logger.debug(String.format("Overriding alternative name using [%s], overridden name was [%s]",
							altName, oldAltName));
				}
				return;
			}

			functions.put(name, fnc);
			alternativeNames.put(name,
					StringHelper.hasLength(altName) ?
							with(logger).trace(String.format("Using alternative name [%s] on property [%s]", altName, name), altName) :
							name);
		});
		functionsMap.entrySet().forEach(entry -> {
			metadata.getPropertyNames().stream().forEach(propName -> {
				entry.getValue().computeIfAbsent(propName,
						key -> with(logger)
								.trace(String.format("[%s#%s]: Masking property [%s]", entityClass.getName(), entry.getKey(), propName),
										MASKER));
			});
		});
		alternativeNamesMap.values().forEach(propNames -> {
			metadata.getPropertyNames().stream().forEach(propName -> {
				propNames.computeIfAbsent(propName, key -> propName);
				propNames.merge(propName, propName, (o, n) -> Optional.ofNullable(o).orElse(n));
			});
		});

		Stream.of(Role.values()).forEach(role -> {
			functionsMap.computeIfAbsent(role, key -> {
				return metadata.getPropertyNames().stream()
						.map(prop -> Map.entry(prop, with(logger)
														.trace(String.format("[%s#%s]: Masking property [%s]", entityClass.getName(), role, prop),
																MASKER)))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			});
			alternativeNamesMap.computeIfAbsent(role, key -> {
				return metadata.getPropertyNames().stream()
						.map(prop -> Map.entry(prop, prop))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			});
		});
		originalNamesByAlternativeNames = alternativeNamesMap.entrySet().stream()
				.map(entry -> Map.entry(entry.getKey(),
						entry.getValue().entrySet().stream()
								.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey))))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		// @formatter:on
		if (!metadata.getPropertyNames().isEmpty()) {
			functionsMap.put(null, metadata.getPropertyNames().stream().map(prop -> Map.entry(prop, MASKER))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
			alternativeNamesMap.put(null, metadata.getPropertyNames().stream().map(prop -> Map.entry(prop, prop))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
		}

		this.functionsMap = Collections.unmodifiableMap(functionsMap);
		this.alternativeNamesByOriginalNames = Collections.unmodifiableMap(alternativeNamesMap);
		properties = metadata.getPropertyNames().toArray(String[]::new);

		LoggerFactory.getLogger(this.getClass()).debug(String.format("\n" + "%s:\n" + "\t%s", entityClass.getName(),
				functionsMap.entrySet().stream().map(entry -> entry.getValue().entrySet().stream()
						.map(fEntry -> String.format("%s:\t[%s] -> [%s]", entry.getKey(),
								this.alternativeNamesByOriginalNames.get(entry.getKey()).get(fEntry.getKey()),
								fEntry.getValue().equals(MASKER) ? "masked"
										: fEntry.getValue().equals(PUBLISHER) ? "published" : fEntry.getValue()))
						.collect(Collectors.joining("\n\t"))).collect(Collectors.joining("\n\t"))));
	}

	private Map<String, Object> produceRow(String[] originalPropNames, Object[] values, Role role) {
		Map<String, Function<Object, Object>> functionsByRole = functionsMap.get(role);
		Map<String, String> alternativeNamesByRole = alternativeNamesByOriginalNames.get(role);

		try {
			int span = values.length;

			return IntStream.range(0, span).mapToObj(index -> {
				String originalPropName = originalPropNames[index];

				return Entry.entry(alternativeNamesByRole.get(originalPropName),
						functionsByRole.get(originalPropName).apply(values[index]));
			}).collect(
			// @formatter:off
					HashMap<String, Object>::new,
					(map, entry) -> map.put(entry.getKey(), entry.getValue()),
					HashMap::putAll);
					// @formatter:on
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			// This could be caused when passing properties in which are not contained in
			// this producer. Seriously though, this should be avoided by devs
			return new HashMap<>(0, LOAD_FACTOR);
		} catch (Exception any) {
			any.printStackTrace(); // should be avoided
			return new HashMap<>(0, LOAD_FACTOR);
		}
	}

	@Override
	public Map<String, Object> produce(Object[] source, Role role) {
		return produce(source, role, properties);
	}

	@Override
	public Map<String, Object> produce(Object[] source, Role role, String[] columnNames) {
		return produceRow(columnNames, source, role);
	}

	@Override
	public List<Map<String, Object>> produce(List<Object[]> source, Role role) {
		return produce(source, role, properties);
	}

	@Override
	public List<Map<String, Object>> produce(List<Object[]> source, Role role, String[] columnNames) {
		// @formatter:off
		return IntStream.range(0, source.size())
				.mapToObj(index -> produceRow(columnNames, source.get(index), role))
				.collect(Collectors.toList());
		// @formatter:on
	}

	@Override
	public String[] validateAndTranslateColumnNames(Role role, String[] requestedColumns)
			throws SQLSyntaxErrorException {
		Map<String, String> alternativeName = originalNamesByAlternativeNames.get(role);
		String[] translatedColumnNames = new String[requestedColumns.length];
		int i = 0;
		String translatedColumn;

		for (String requestedColumn : requestedColumns) {
			if ((translatedColumn = alternativeName.get(requestedColumn)) == null) {
				throw new SQLSyntaxErrorException(String.format("Unknown property [%s]", requestedColumn));
			}

			translatedColumnNames[i++] = translatedColumn;
		}

		return translatedColumnNames;
	}

}
