/**
 * 
 */
package adn.model.factory.authentication.dynamic;

import static adn.helpers.LoggerHelper.with;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.application.context.builders.ModelContextProvider;
import adn.helpers.StringHelper;
import adn.helpers.TypeHelper;
import adn.model.DomainEntity;
import adn.model.entities.metadata.DomainEntityMetadata;
import adn.model.factory.authentication.Arguments;
import adn.model.factory.authentication.BatchedPojoSource;
import adn.model.factory.authentication.BatchedSource;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.DynamicMapModelProducerFactory;
import adn.model.factory.authentication.SecuredProperty;
import adn.model.factory.authentication.SinglePojoSource;
import adn.model.factory.authentication.SingleSource;
import adn.model.factory.authentication.SourceArguments;

/**
 * @author Ngoc Huy
 *
 */
public class DynamicMapModelProducerImpl<T extends DomainEntity> implements DynamicMapModelProducer<T> {

	private static final Function<Arguments<?>, Arguments<?>> ARGUMENT_PRESERVER = (arg) -> arg;

	private static final float LOAD_FACTOR = 1.175f;

	private final Map<String, Map<String, BiFunction<Arguments<?>, Credential, ?>>> producingFunctions;
	private final Map<String, Map<String, String>> aliasMap;
	private final Map<String, Map<String, String>> namesMap;

	private final DynamicMapModelProducerFactory factory;
	private final ModelContextProvider modelContext;
	private final DomainEntityMetadata metadata;

	/**
	 * 
	 */
	public DynamicMapModelProducerImpl(Class<T> entityType, Set<SecuredProperty<T>> properties,
			ModelContextProvider modelContext, DynamicMapModelProducerFactory factory) {
		this.modelContext = modelContext;
		this.factory = factory;
		metadata = modelContext.getMetadata(entityType);

		Logger logger = LoggerFactory.getLogger(this.getClass());
		Map<String, Map<String, BiFunction<Arguments<?>, Credential, ?>>> producingFunctions = new HashMap<>(0);
		Map<String, Map<String, String>> aliasMap = new HashMap<>(0);
		Map<String, Map<String, String>> originalNamesMap = new HashMap<>(0);
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
			BiFunction<Arguments<?>, Credential, ?> function = prop.getFunction();
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
			
			Map<String, BiFunction<Arguments<?>, Credential, ?>> functions = producingFunctions.get(credential);
			Map<String, String> aliases = aliasMap.get(credential);
			
			if (functions.containsKey(name)) {
				if (functions.get(name) != function) {
					BiFunction<Arguments<?>, Credential, ?> oldFnc = functions.put(name, function);
					
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

		this.producingFunctions = producingFunctions;
		this.aliasMap = Collections.unmodifiableMap(aliasMap);
		this.namesMap = Collections.unmodifiableMap(originalNamesMap);
	}

	@Override
	public Map<String, Object> produce(Object[] source, Credential credential) {
		return null;
	}

	@Override
	public List<Map<String, Object>> produce(List<Object[]> source, Credential credential) {
		return null;
	}

	// @formatter:off
	private Map<String, Object> produceRow(
			String[] columns,
			Object[] values,
			Credential credential,
			Map<String, BiFunction<Arguments<?>, Credential, ?>> functions,
			Map<String, String> alias
			) {
		// @formatter:on
		try {
			return IntStream.of(0, columns.length).mapToObj(index -> {
				String name = columns[index];

				return Map.entry(alias.get(name),
						(Object) functions.get(name).apply(argument(values[index]), credential));
			}).collect(HashMap<String, Object>::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()),
					HashMap::putAll);
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

	// @formatter:off
	private Map<String, Object> produceRow(
			String[] columns,
			Object[] values,
			Credential credential,
			Map<String, BiFunction<Arguments<?>, Credential, ?>> functions,
			Map<String, String> alias,
			Map<String, Function<Arguments<?>, ? extends Arguments<?>>> argumentResolvers
			) {
		// @formatter:on
		try {
			return IntStream.of(0, columns.length).mapToObj(index -> {
				String name = columns[index];

				return Map.entry(alias.get(name), (Object) functions.get(name)
						.apply(argumentResolvers.get(name).apply(argument(values[index])), credential));
			}).collect(HashMap<String, Object>::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()),
					HashMap::putAll);
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			return new HashMap<>(0, LOAD_FACTOR);
		} catch (Exception any) {
			any.printStackTrace();
			return new HashMap<>(0, LOAD_FACTOR);
		}
	}

	private Map<String, BiFunction<Arguments<?>, Credential, ?>> getFunctions(String evaluation)
			throws UnauthorizedCredential {
		Map<String, BiFunction<Arguments<?>, Credential, ?>> functions = producingFunctions.get(evaluation);

		if (functions == null) {
			throw new UnauthorizedCredential(evaluation);
		}

		return functions;
	}

	@Override
	public Map<String, Object> produceSingleSource(SingleSource source, Credential credential)
			throws UnauthorizedCredential {
		String evaluation = credential.evaluate();
		Map<String, BiFunction<Arguments<?>, Credential, ?>> functions = getFunctions(evaluation);
		Map<String, String> alias = aliasMap.get(evaluation);

		if (!source.hasAssociation()) {
			return produceRow(source.getColumns(), source.getSource(), credential, functions, alias);
		}

		Object[] values = source.getSource();
		String[] columns = source.getColumns();
		Set<Integer> associationIndicies = source.getAssociationIndicies();
		AssociationProducingMethodArguments producingMethodArguments = prepareAssociationProducing(values, columns,
				associationIndicies, source, functions);

		return produceRow(columns, values, credential, producingMethodArguments.functions, alias,
				producingMethodArguments.argumentsResolvers);
	}

	@SuppressWarnings("unchecked")
	// @formatter:off
	private AssociationProducingMethodArguments prepareAssociationProducing(
			Object[] values,
			String[] columns,
			Set<Integer> associationIndicies,
			SourceArguments<?> source,
			Map<String, BiFunction<Arguments<?>, Credential, ?>> configuredFunctions) {
		// @formatter:on
		Map<String, BiFunction<Arguments<?>, Credential, ?>> copiedFunctions = Map.copyOf(configuredFunctions);
		int span = columns.length;
		Map<String, Function<Arguments<?>, ? extends Arguments<?>>> argumentsResolvers = new HashMap<>(columns.length,
				LOAD_FACTOR);

		IntStream.range(0, span).forEach(index -> {
			if (associationIndicies.contains(index)) {
				argumentsResolvers.put(columns[index], ARGUMENT_PRESERVER);
				return;
			}

			String column = columns[index];
			Object value = values[index];
			Class<?> valueType = value.getClass();
			DynamicMapModelProducer<DomainEntity> associationProducer;
			// represents an Object[]
			if (valueType.isArray()) {
				Class<DomainEntity> associationType = (Class<DomainEntity>) metadata.getPropertyType(column);

				associationProducer = factory.getProducers(associationType);
				copiedFunctions.put(column, (arg, credential) -> {
					try {
						return associationProducer.produceSingleSource((SingleSource) arg, credential);
					} catch (UnauthorizedCredential e) {
						return null;
					}
				});
				argumentsResolvers.put(column, (Function<Arguments<?>, ? extends Arguments<?>>) new SingleSourceImpl(
						source.getAssociationColumns(index), metadata, (Object[]) value));
				return;
			}
			// a POJO
			if (DomainEntity.class.isAssignableFrom(valueType)) {
				Class<DomainEntity> associationType = (Class<DomainEntity>) metadata.getPropertyType(column);

				associationProducer = factory.getProducers(associationType);
				copiedFunctions.put(column, (arg, credential) -> {
					try {
						return associationProducer.produceSinglePojo((SinglePojoSource<? extends DomainEntity>) arg,
								credential);
					} catch (UnauthorizedCredential e) {
						return null;
					}
				});
				argumentsResolvers.put(column,
						(Function<Arguments<?>, ? extends Arguments<?>>) new SinglePojoSourceImpl<DomainEntity>(
								source.getAssociationColumns(index), associationType,
								modelContext.getMetadata(associationType), (DomainEntity) value));
				return;
			}
			// collection of POJOs
			if (Collection.class.isAssignableFrom(valueType)) {
				Class<DomainEntity> associationType = (Class<DomainEntity>) metadata.getPropertyType(column);
				Class<?> genericType = ((Class<?>) TypeHelper.getGenericType(valueType));

				associationProducer = factory.getProducers(associationType);

				if (Object[].class.isAssignableFrom(genericType)) {
					copiedFunctions.put(column, (arg, credential) -> {
						try {
							return associationProducer
									.produceBatchedPojo((BatchedPojoSource<? extends DomainEntity>) arg, credential);
						} catch (UnauthorizedCredential e) {
							return null;
						}
					});
					argumentsResolvers.put(column,
							(Function<Arguments<?>, ? extends Arguments<?>>) new BatchedPojoSourceImpl<>(
									source.getAssociationColumns(index), modelContext.getMetadata(associationType),
									associationType, new ArrayList<>((Collection<DomainEntity>) value)));
					return;
				}

				if (DomainEntity.class.isAssignableFrom(valueType)) {
					copiedFunctions.put(column, (arg, credential) -> {
						try {
							return associationProducer.produceBatchedSource((BatchedSource) arg, credential);
						} catch (UnauthorizedCredential e) {
							return null;
						}
					});
					argumentsResolvers.put(column,
							(Function<Arguments<?>, ? extends Arguments<?>>) new BatchedSourceImpl(
									source.getAssociationColumns(index), modelContext.getMetadata(associationType),
									new ArrayList<>((Collection<Object[]>) value)));
				}

				throw new IllegalArgumentException(String.format("Unnknown association type %s", genericType));
			}

			throw new IllegalArgumentException(String.format("Unnknown association type %s", valueType));
		});

		return new AssociationProducingMethodArguments(copiedFunctions, argumentsResolvers);
	}

	@Override
	public List<Map<String, Object>> produceBatchedSource(BatchedSource sourceBatch, Credential credential)
			throws UnauthorizedCredential {
		List<Object[]> sources = sourceBatch.getSource();
		String evaluation = credential.evaluate();
		Map<String, BiFunction<Arguments<?>, Credential, ?>> functions = getFunctions(evaluation);
		Map<String, String> alias = aliasMap.get(evaluation);
		String[] columns = sourceBatch.getColumns();

		int batchSize = sources.size();

		if (!sourceBatch.hasAssociation()) {
			return IntStream.range(0, batchSize)
					.mapToObj(index -> produceRow(columns, sources.get(index), credential, functions, alias))
					.collect(Collectors.toList());
		}

		Set<Integer> associationIndicies = sourceBatch.getAssociationIndicies();
		AssociationProducingMethodArguments producingMethodArguments = prepareAssociationProducing(sources.get(0),
				columns, associationIndicies, sourceBatch, functions);

		return IntStream.range(0, batchSize)
				.mapToObj(index -> produceRow(columns, sources.get(index), credential,
						producingMethodArguments.functions, alias, producingMethodArguments.argumentsResolvers))
				.collect(Collectors.toList());
	}

	@Override
	public <E extends T> Map<String, Object> produceSinglePojo(SinglePojoSource<E> source, Credential credential) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends T> List<Map<String, Object>> produceBatchedPojo(BatchedPojoSource<E> sourceBatch,
			Credential credential) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> validateColumns(Credential credential, Collection<String> columns) throws NoSuchFieldException {
		String evaluation = credential.evaluate();
		Map<String, String> name = namesMap.get(evaluation);
		List<String> translatedColumnNames = new ArrayList<>(columns.size());
		String translatedColumn;

		for (String requestedColumn : columns) {
			if ((translatedColumn = name.get(requestedColumn)) == null) {
				throw new NoSuchFieldException(String.format("Unknown property [%s]", requestedColumn));
			}

			translatedColumnNames.add(translatedColumn);
		}

		return translatedColumnNames;
	}

	@Override
	public void afterFactoryBuild(DynamicMapModelProducerFactory factory) {}

	static <T> BasicArgument<T> argument(T value) {
		return new BasicArgument<T>(value);
	}

	private static class BasicArgument<Y> implements Arguments<Y> {

		private final Y value;

		private BasicArgument(Y value) {
			super();
			this.value = value;
		}

		@Override
		public Y getSource() {
			return value;
		}

		@Override
		public <X extends Arguments<Y>, E extends X> E unwrap(Class<E> type) {
			throw new UnsupportedOperationException("This instance is type of a final type");
		}

	}

	private class AssociationProducingMethodArguments {

		Map<String, BiFunction<Arguments<?>, Credential, ?>> functions;
		Map<String, Function<Arguments<?>, ? extends Arguments<?>>> argumentsResolvers;

		public AssociationProducingMethodArguments(Map<String, BiFunction<Arguments<?>, Credential, ?>> functions,
				Map<String, Function<Arguments<?>, ? extends Arguments<?>>> argumentsResolvers) {
			super();
			this.functions = functions;
			this.argumentsResolvers = argumentsResolvers;
		}

	}

}
