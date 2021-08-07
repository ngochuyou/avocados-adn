/**
 * 
 */
package adn.model.factory.property.production.department;

import static adn.helpers.LoggerHelper.with;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.property.access.spi.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.application.context.ContextProvider;
import adn.helpers.TypeHelper;
import adn.helpers.Utils;
import adn.helpers.Utils.Entry;
import adn.model.DepartmentScoped;
import adn.model.DomainEntity;
import adn.model.ModelContextProvider;
import adn.model.entities.Entity;
import adn.model.entities.metadata.DomainEntityMetadata;
import adn.model.factory.property.production.DepartmentBasedModelPropertiesProducer;
import adn.model.factory.property.production.DepartmentScopedProperty;
import adn.model.factory.property.production.ModelPropertiesProducer;

/**
 * @author Ngoc Huy
 *
 */
public class DepartmentBasedModelPropertiesProducerImpl<T extends DepartmentScoped>
		implements DepartmentBasedModelPropertiesProducer {

	private static final float LOAD_FACTOR = 1.175f;
	private static final BiFunction<Object, UUID, Object> SOURCE_PRESERVER = (source, role) -> source;
	private static final BiFunction<Object, UUID, Object> SOURCE_TRANSLATOR = (source, role) -> Entry.entry(source,
			role);

	private Class<T> type;
	private Map<UUID, String> departmentNames;

	private final Map<UUID, Map<String, Function<Object, Object>>> functionsMap;
	private final Map<UUID, Map<String, BiFunction<Object, UUID, Object>>> sourceTranslators;
	private final Map<String, Getter> nonLazyPropertyGetters;
	private final Set<String> registeredProperties;

	@SuppressWarnings("unchecked")
	public DepartmentBasedModelPropertiesProducerImpl(Class<T> type, Set<DepartmentScopedProperty<T>> properties) {
		this.type = type;

		Map<UUID, Map<String, Function<Object, Object>>> functionsMap = new HashMap<>();
		final Logger logger = LoggerFactory.getLogger(this.getClass());
		DomainEntityMetadata metadata = ContextProvider.getBean(ModelContextProvider.class)
				.getMetadata((Class<DomainEntity>) type);
		// @formatter:off
		properties.stream().filter(prop -> {
			boolean isParent = TypeHelper.isParentOf(prop.getEntityType(), type);
			boolean unknownProperty = !metadata.hasProperty(prop.getName());
			boolean nullFunction = prop.getFunction() == null;

			if (!isParent || unknownProperty || nullFunction) {
				logger.debug(String.format("[%s]: Ignoring %s [%s] %s", type.getName(),
						DepartmentScopedProperty.class.getSimpleName(), prop.getName(),
						!isParent ? "invalid type" : unknownProperty ? "unknown property" : " null function"));
				return false;
			}

			return true;
		}).sorted((o, t) -> o.getEntityType().equals(t.getEntityType()) ? 0
				: (TypeHelper.isExtendedFrom(o.getEntityType(), t.getEntityType()) ? 1 : -1)
		).forEach(prop -> {
			String name = prop.getName();
			UUID departmentId = prop.getDepartmentId();
			Function<Object, Object> fnc = prop.getFunction();
			
			if (!functionsMap.containsKey(departmentId)) {
				functionsMap.put(departmentId, Stream.of(Map.entry(name, fnc)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
				return;
			}
			
			Map<String, Function<Object, Object>> functions = functionsMap.get(departmentId);
			
			if (functions.containsKey(name)) {
				if (functions.get(name) != fnc) {
					logger.debug(
							String.format("[%s]: Overriding function [%s] for property [%s] on department id [%s]",
									type.getName(), fnc, name, departmentId));
					functions.put(name, fnc);
					return;
				}
			}
			
			functions.put(name, fnc);
		});		
		// @formatter:on
		functionsMap.entrySet().stream().forEach(entry -> {
			metadata.getPropertyNames().stream().forEach(propName -> {
				entry.getValue().computeIfAbsent(propName,
						key -> with(logger)
								.debug(String.format("[%s]: Masking property [%s] for against department id [%s]",
										type.getName(), propName, entry.getKey()), MASKER));
			});
		});

		SessionFactory sf = ContextProvider.getBean(SessionFactory.class);
		Session ss = sf.openSession();

		ss.setDefaultReadOnly(true);
		ss.setHibernateFlushMode(FlushMode.MANUAL);

		List<Object[]> departmentList = ss.createQuery("SELECT d.id, d.name FROM Department d", Object[].class)
				.getResultList();

		ss.clear();
		ss.close();

		departmentList.stream().forEach(cols -> {
			UUID departmentId = (UUID) cols[0];

			functionsMap.computeIfAbsent(departmentId,
					key -> with(logger).debug(
							String.format("[%s]: Masking every properties against department id [%s]", type.getName(),
									departmentId),
							metadata.getPropertyNames().stream().map(propName -> Map.entry(propName, MASKER))
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
		});

		if (!metadata.getPropertyNames().isEmpty()) {
			functionsMap.put(null,
					with(logger).debug(String.format("[%s]: Supporting null key entry with MASKER", type.getName()),
							metadata.getPropertyNames().stream().map(propName -> Map.entry(propName, MASKER))
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
		}

		departmentNames = departmentList.stream().map(cols -> Map.entry((UUID) cols[0], (String) cols[1]))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		this.registeredProperties = metadata.getPropertyNames();
		this.functionsMap = functionsMap;
		sourceTranslators = departmentNames.keySet().stream()
				.map(departmentId -> Map.entry(departmentId,
						metadata.getPropertyNames().stream().map(propName -> Map.entry(propName, SOURCE_PRESERVER))
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		Set<String> nonLazyProperties = metadata.getNonLazyPropertyNames();

		nonLazyPropertyGetters = metadata.getGetters().stream()
				.filter(entry -> nonLazyProperties.contains(entry.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	// @formatter:off
	private Map<String, Object> produceRow(
			Object[] source,
			String[] columns,
			Map<String, Function<Object, Object>> functions,
			Map<String, BiFunction<Object, UUID, Object>> sourceTranslators,
			UUID departmentId) {
	// @formatter:on
		try {
			int span = columns.length;

			return IntStream.range(0, span).mapToObj(index -> {
				String propName = columns[index];

				return Entry.entry(propName, functions.get(propName)
						.apply(sourceTranslators.get(propName).apply(source[index], departmentId)));
			}).collect(
			// @formatter:off
				HashMap<String, Object>::new,
				(map, entry) -> map.put(entry.getKey(), entry.getValue()),
				HashMap::putAll
			// @formatter:on
			);
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			return new HashMap<>(0, LOAD_FACTOR);
		} catch (Exception any) {
			any.printStackTrace();
			return new HashMap<>(0, LOAD_FACTOR);
		}
	}

	@Override
	public Map<String, Object> produce(Object[] source, String[] columns, UUID departmentId) {
		return produceRow(source, columns, functionsMap.get(departmentId), sourceTranslators.get(departmentId),
				departmentId);
	}

	@Override
	public List<Map<String, Object>> produce(List<Object[]> sources, String[] columns, UUID departmentId) {
		Map<String, Function<Object, Object>> functions = functionsMap.get(departmentId);
		Map<String, BiFunction<Object, UUID, Object>> translators = sourceTranslators.get(departmentId);
		// @formatter:off
		return sources
				.stream().map(source -> produceRow(source, columns, functions, translators, departmentId))
				.collect(Collectors.toList());
		// @formatter:on
	}

	@Override
	public Map<String, Object> produce(Object[] source, UUID departmentId) {
		return produce(source, registeredProperties.toArray(new String[registeredProperties.size()]), departmentId);
	}

	@Override
	public List<Map<String, Object>> produce(List<Object[]> sources, UUID departmentId) {
		return produce(sources, registeredProperties.toArray(new String[registeredProperties.size()]), departmentId);
	}

	@Override
	public Map<String, Object> produce(Object[] source) {
		return produce(source, null);
	}

	@Override
	public List<Map<String, Object>> produce(List<Object[]> sources) {
		return produce(sources, null);
	}

	@Override
	public Collection<String> validateAndTranslateColumnNames(Collection<String> requestedColumns)
			throws NoSuchFieldException {
		for (String requestedColumn : requestedColumns) {
			if (!registeredProperties.contains(requestedColumn)) {
				throw new NoSuchFieldException(String.format("Unknown property [%s]", requestedColumn));
			}
		}

		return requestedColumns;
	}

	@Override
	public Map<String, Object> singularProduce(Object source) {
		throw new UnsupportedOperationException("Singular production with no column name is not supported");
	}

	@Override
	public List<Map<String, Object>> singularProduce(List<Object> source) {
		throw new UnsupportedOperationException("Singular production with no column name is not supported");
	}

	@Override
	public Map<String, Object> singularProduce(Object source, String columnName, UUID departmentId) {
		Map<String, Function<Object, Object>> functions = functionsMap.get(departmentId);
		Map<String, Object> result = new HashMap<>();

		result.put(columnName, functions.get(columnName)
				.apply(sourceTranslators.get(departmentId).get(columnName).apply(source, departmentId)));

		return result;
	}

	@Override
	public List<Map<String, Object>> singularProduce(List<Object> sources, String columnName, UUID departmentId) {
		Function<Object, Object> function = functionsMap.get(departmentId).get(columnName);
		BiFunction<Object, UUID, Object> sourceTranslator = sourceTranslators.get(departmentId).get(columnName);

		return IntStream.range(0, sources.size()).mapToObj(index -> {
			Map<String, Object> result = new HashMap<>();

			result.put(columnName, function.apply(sourceTranslator.apply(sources.get(index), departmentId)));

			return result;
		}).collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void afterFactoryBuild(
			Map<Class<? extends DepartmentScoped>, DepartmentBasedModelPropertiesProducer> producers) {
		DomainEntityMetadata metadata = ContextProvider.getBean(ModelContextProvider.class)
				.getMetadata((Class<DomainEntity>) type);
		Set<String> associations = registeredProperties.stream().filter(metadata::isEntityType)
				.collect(Collectors.toSet());
		String propName;

		for (Map.Entry<UUID, Map<String, Function<Object, Object>>> entry : functionsMap.entrySet()) {
			for (Map.Entry<String, Function<Object, Object>> function : entry.getValue().entrySet().stream()
					.filter(propBased -> associations.contains(propBased.getKey())).collect(Collectors.toSet())) {
				propName = function.getKey();

				if (function.getValue() == PUBLISHER) {
					DepartmentBasedModelPropertiesProducer associationProducer = producers
							.get(metadata.getPropertyType(propName));

					if (!this.getClass().isAssignableFrom(associationProducer.getClass())) {
						throw new IllegalArgumentException(
								String.format("Unqualified producer of type: [%s]", associationProducer.getClass()));
					}

					DepartmentBasedModelPropertiesProducerImpl<? extends Entity> producer = (DepartmentBasedModelPropertiesProducerImpl<? extends Entity>) associationProducer;

					entry.getValue().put(propName, producer::produceAssociation);
					sourceTranslators.get(entry.getKey()).put(propName, SOURCE_TRANSLATOR);
				}
			}
		}

		final Logger logger = LoggerFactory.getLogger(this.getClass());

		logger.trace(String.format("%s\n" + "\t%s", type.getName(),
				functionsMap.entrySet().stream()
						.map(entry -> entry.getValue().entrySet().stream()
								.map(innerEntry -> String.format("[%s#%s] -> [%s]", departmentNames.get(entry.getKey()),
										innerEntry.getKey(),
										innerEntry.getValue() == ModelPropertiesProducer.MASKER ? "mask"
												: innerEntry.getValue() == ModelPropertiesProducer.PUBLISHER ? "publish"
														: innerEntry.getValue()))
								.collect(Collectors.joining("\n\t")))
						.collect(Collectors.joining("\n\t"))));

		type = null;
		departmentNames = null;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> produceAssociation(Object sourceEntry) {
		Utils.Entry<Object, UUID> valueEntry = (Utils.Entry<Object, UUID>) sourceEntry;
		T entity = (T) valueEntry.getKey();
		Map<String, Object> valueMap = nonLazyPropertyGetters.entrySet().stream()
				.map(entry -> Utils.Entry.entry(entry.getKey(), entry.getValue().get(entity)))
				.collect(HashMap<String, Object>::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()),
						HashMap::putAll);

		return produce(valueMap.values().toArray(), valueMap.keySet().toArray(String[]::new), valueEntry.getValue());
	}

}
