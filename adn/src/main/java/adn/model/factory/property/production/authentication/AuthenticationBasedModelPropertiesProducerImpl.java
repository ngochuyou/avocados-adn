/**
 * 
 */
package adn.model.factory.property.production.authentication;

import static adn.helpers.LoggerHelper.with;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.hibernate.property.access.spi.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.application.context.ContextProvider;
import adn.application.context.builders.ModelContextProvider;
import adn.helpers.StringHelper;
import adn.helpers.TypeHelper;
import adn.helpers.Utils;
import adn.helpers.Utils.Entry;
import adn.model.DomainEntity;
import adn.model.entities.Entity;
import adn.model.entities.metadata.DomainEntityMetadata;
import adn.model.factory.AuthenticationBasedModelPropertiesProducer;
import adn.model.factory.property.production.SecuredProperty;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public class AuthenticationBasedModelPropertiesProducerImpl<T extends DomainEntity>
		implements AuthenticationBasedModelPropertiesProducer {

	private static final float LOAD_FACTOR = 1.175f;
	private static final BiFunction<Object, Role, Object> SOURCE_PRESERVER = (source, role) -> source;
	private static final BiFunction<Object, Role, Object> SOURCE_TRANSLATOR = (source, role) -> Entry.entry(source, role);

	private Class<T> type; // only needed for afterFactoryBuild, will release once done

	private final Map<Role, Map<String, Function<Object, Object>>> functionsMap;
	private final Map<Role, Map<String, String>> alternativeNamesByOriginalNames;
	private final Map<Role, Map<String, String>> originalNamesByAlternativeNames;
	private final Map<Role, Map<String, BiFunction<Object, Role, Object>>> sourceTranslators;
	private final Map<String, Getter> getters;
	private final String[] properties;

	public AuthenticationBasedModelPropertiesProducerImpl(Class<T> entityClass,
			Set<SecuredProperty<T>> securedProperties) {
		type = entityClass;

		final Logger logger = LoggerFactory.getLogger(this.getClass());
		DomainEntityMetadata metadata = ContextProvider.getBean(ModelContextProvider.class).getMetadata(entityClass);
		Map<Role, Map<String, Function<Object, Object>>> functionsMap = new HashMap<>(0, 1f);
		Map<Role, Map<String, String>> alternativeNamesMap = new HashMap<>(0, 1f);
		// @formatter:off
		securedProperties.stream().filter(prop -> {
			boolean isParent = TypeHelper.isParentOf(prop.getEntityType(), entityClass);
			boolean unknownProperty = !metadata.hasProperty(prop.getPropertyName());
			boolean nullFunction = prop.getFunction() == null;
			
			if (!isParent || unknownProperty || nullFunction) {
				logger.trace(String.format("[%s]: Ignoring %s [%s] %s", entityClass.getName(),
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
						Stream.of(Map.entry(name, StringHelper.hasLength(altName) ? with(logger).debug(String.format("Using alternative name [%s] on property [%s]", altName, name), altName) : name))
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
							with(logger).debug(String.format("Using alternative name [%s] on property [%s]", altName, name), altName) :
							name);
			return;
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

		this.functionsMap = functionsMap;
		this.alternativeNamesByOriginalNames = alternativeNamesMap;
		properties = metadata.getPropertyNames().toArray(String[]::new);

		Set<String> nonLazyProperties = metadata.getNonLazyPropertyNames();

		this.getters = metadata.getGetters().stream().filter(entry -> nonLazyProperties.contains(entry.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		this.sourceTranslators = Stream.of(Role.values())
				.map(role -> Map.entry(role,
						metadata.getPropertyNames().stream().map(propName -> Map.entry(propName, SOURCE_PRESERVER))
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	// @formatter:off
	private Map<String, Object> produceRow(
			String[] originalPropNames,
			Object[] values,
			Map<String, Function<Object, Object>> functionsByRole,
			Map<String, String> alternativeNamesByRole,
			Map<String, BiFunction<Object, Role, Object>> sourceTranslatorsByRole,
			Role role) {
	// @formatter:on
		try {
			int span = originalPropNames.length;

			return IntStream.range(0, span).mapToObj(index -> {
				String originalPropName = originalPropNames[index];

				return Entry.entry(alternativeNamesByRole.get(originalPropName), // (1) the property name
						functionsByRole.get(originalPropName).apply( // (3) produce the secured value
								sourceTranslatorsByRole.get(originalPropName) // (2) translate the value in to itself or
										.apply(values[index], role))); // a pair consists of itself and a role
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
			// should be avoided
			any.printStackTrace();
			return new HashMap<>(0, LOAD_FACTOR);
		}
	}

	@Override
	public Map<String, Object> produce(Object[] source, Role role) {
		return produce(source, role, properties);
	}

	@Override
	public Map<String, Object> produce(Object[] source, Role role, String[] columnNames) {
		return produceRow(columnNames, source, functionsMap.get(role), alternativeNamesByOriginalNames.get(role),
				sourceTranslators.get(role), role);
	}

	@Override
	public List<Map<String, Object>> produce(List<Object[]> source, Role role) {
		return produce(source, role, properties);
	}

	@Override
	public List<Map<String, Object>> produce(List<Object[]> source, Role role, String[] columnNames) {
		Map<String, Function<Object, Object>> functionsByRole = functionsMap.get(role);
		Map<String, String> alternativeNamesByRole = alternativeNamesByOriginalNames.get(role);
		Map<String, BiFunction<Object, Role, Object>> sourceTranslatorsByRole = sourceTranslators.get(role);
		// @formatter:off
		return IntStream.range(0, source.size())
				.mapToObj(index -> produceRow(columnNames, source.get(index), functionsByRole, alternativeNamesByRole, sourceTranslatorsByRole, role))
				.collect(Collectors.toList());
		// @formatter:on
	}

	@Override
	public Collection<String> validateAndTranslateColumnNames(Role role, Collection<String> requestedColumns)
			throws NoSuchFieldException {
		Map<String, String> alternativeName = originalNamesByAlternativeNames.get(role);
		List<String> translatedColumnNames = new ArrayList<>(requestedColumns.size());
		String translatedColumn;

		for (String requestedColumn : requestedColumns) {
			if ((translatedColumn = alternativeName.get(requestedColumn)) == null) {
				throw new NoSuchFieldException(String.format("Unknown property [%s]", requestedColumn));
			}

			translatedColumnNames.add(translatedColumn);
		}

		return translatedColumnNames;
	}

	@Override
	public Map<String, Object> singularProduce(Object source) {
		throw new UnsupportedOperationException("Singular production wiht no column name is not supported");
	}

	@Override
	public List<Map<String, Object>> singularProduce(List<Object> source) {
		throw new UnsupportedOperationException("Singular production wiht no column name is not supported");
	}

	@Override
	public Map<String, Object> singularProduce(Object source, Role role, String columnName) {
		Map<String, Function<Object, Object>> authenticatedFunctions = functionsMap.get(role);
		Map<String, String> authenticatedAlternativeNames = alternativeNamesByOriginalNames.get(role);
		Map<String, Object> result = new HashMap<>();

		result.put(authenticatedAlternativeNames.get(columnName), authenticatedFunctions.get(columnName)
				.apply(sourceTranslators.get(role).get(columnName).apply(source, role)));

		return result;
	}

	@Override
	public List<Map<String, Object>> singularProduce(List<Object> source, Role role, String columnName) {
		Function<Object, Object> authenticatedFunction = functionsMap.get(role).get(columnName);
		String authenticatedAlternativeName = alternativeNamesByOriginalNames.get(role).get(columnName);
		BiFunction<Object, Role, Object> sourceTranslator = sourceTranslators.get(role).get(columnName);

		return IntStream.range(0, source.size()).mapToObj(index -> {
			Map<String, Object> result = new HashMap<>();

			result.put(authenticatedAlternativeName,
					authenticatedFunction.apply(sourceTranslator.apply(source.get(index), role)));

			return result;
		}).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void afterFactoryBuild(
			Map<Class<? extends DomainEntity>, AuthenticationBasedModelPropertiesProducer> producers) {
		DomainEntityMetadata metadata = ContextProvider.getBean(ModelContextProvider.class).getMetadata(type);
		Set<String> associations = Stream.of(properties).filter(metadata::isEntityType).collect(Collectors.toSet());
		String propName;

		for (Map.Entry<Role, Map<String, Function<Object, Object>>> entry : functionsMap.entrySet()) {
			for (Map.Entry<String, Function<Object, Object>> function : entry.getValue().entrySet().stream()
					.filter(propBased -> associations.contains(propBased.getKey())).collect(Collectors.toSet())) {
				propName = function.getKey();

				if (function.getValue() == PUBLISHER) {
					AuthenticationBasedModelPropertiesProducer associationProducer = producers
							.get(metadata.getPropertyType(propName));

					if (!this.getClass().isAssignableFrom(associationProducer.getClass())) {
						throw new IllegalArgumentException(
								String.format("Unqualified producer of type: [%s]", associationProducer.getClass()));
					}

					AuthenticationBasedModelPropertiesProducerImpl<? extends Entity> producer = (AuthenticationBasedModelPropertiesProducerImpl<? extends Entity>) associationProducer;

					entry.getValue().put(propName, producer::produceAssociation);
					sourceTranslators.get(entry.getKey()).put(propName, SOURCE_TRANSLATOR);
				}
			}
		}

		final Logger logger = LoggerFactory.getLogger(this.getClass());

		logger.debug(String.format("\n" + "%s:\n" + "\t%s", type.getName(),
				functionsMap.entrySet().stream().map(entry -> entry.getValue().entrySet().stream()
						.map(fEntry -> String.format("%s:\t[%s] -> [%s]", entry.getKey(),
								this.alternativeNamesByOriginalNames.get(entry.getKey()).get(fEntry.getKey()),
								fEntry.getValue().equals(MASKER) ? "masked"
										: fEntry.getValue().equals(PUBLISHER) ? "published" : fEntry.getValue()))
						.collect(Collectors.joining("\n\t"))).collect(Collectors.joining("\n\t"))));

		type = null;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> produceAssociation(Object sourceEntry) {
		Utils.Entry<Object, Role> valueEntry = (Utils.Entry<Object, Role>) sourceEntry;
		T entity = (T) valueEntry.getKey();
		Map<String, Object> valueMap = getters.entrySet().stream()
				.map(entry -> Utils.Entry.entry(entry.getKey(), entry.getValue().get(entity)))
				.collect(HashMap<String, Object>::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()),
						HashMap::putAll);

		return produce(valueMap.values().toArray(), valueEntry.getValue(), valueMap.keySet().toArray(String[]::new));
	}

}
