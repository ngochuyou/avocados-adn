/**
 * 
 */
package adn.model.factory.authentication.dynamicmap;

import static adn.helpers.LoggerHelper.with;
import static adn.helpers.Utils.Entry.entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.hibernate.property.access.spi.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.helpers.FunctionHelper.HandledBiFunction;
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
import adn.model.factory.authentication.SourceMetadata;
import adn.model.factory.authentication.SourceType;

/**
 * @author Ngoc Huy
 *
 */
public class DynamicMapModelProducerImpl<T extends DomainEntity> implements DynamicMapModelProducer<T> {

	private static final Logger logger = LoggerFactory.getLogger(DynamicMapModelProducerImpl.class);

	private static final float LOAD_FACTOR = 1.175f;

	private final Map<String, Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>>> producingFunctions;
	private final Map<String, Map<String, String>> aliasMap;
	private final Map<String, Map<String, String>> namesMap;
	private final Map<String, Getter> getters;

	private final DynamicMapModelProducerFactory factory;
	private final DomainEntityMetadata<T> entityMetadata;

	/**
	 * 
	 */
	public DynamicMapModelProducerImpl(Class<T> entityType, Set<SecuredProperty<T>> properties,
			DomainEntityMetadata<T> entityMetadata, DynamicMapModelProducerFactory factory) {
		this.factory = factory;
		this.entityMetadata = entityMetadata;

		Logger logger = LoggerFactory.getLogger(this.getClass());
		Map<String, Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>>> producingFunctions = new HashMap<>(
				0);
		Map<String, Map<String, String>> aliasMap = new HashMap<>(0);
		Map<String, Map<String, String>> originalNamesMap = new HashMap<>(0);
		// @formatter:off
		properties.stream().filter(prop -> {
			boolean isParent = TypeHelper.isParentOf(prop.getOwningType(), entityType);
			boolean unknownProperty = !entityMetadata.hasProperty(prop.getName());
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
			HandledBiFunction<Arguments<?>, Credential, ?, Exception> function = prop.getFunction();
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
			
			Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> functions = producingFunctions.get(credential);
			Map<String, String> aliases = aliasMap.get(credential);
			
			if (functions.containsKey(name)) {
				if (functions.get(name) != function) {
					HandledBiFunction<Arguments<?>, Credential, ?, Exception> oldFnc = functions.put(name, function);
					
					logger.debug(String.format("[%s#%s] Overriding function of property [%s] using [%s], overridden function was [%s]",
							entityType, credential, name, function, oldFnc));
				}

				if (!aliases.get(name).equals(alias)) {
					String oldAlias = aliases.put(name, alias);
					
					logger.debug(String.format("[%s]: Overriding alternative name using [%s], overridden name was [%s]",
							credential, alias, oldAlias));
				}
				return;
			}
			
			functions.put(name, function);
			aliases.put(name,
					StringHelper.hasLength(alias) ?
							with(logger).debug(String.format("[%s]: Using alternative name [%s] on property [%s]", credential, alias, name), alias) :
							name);
		});;
		// @formatter:on
		producingFunctions.entrySet().forEach(entry -> {
			entityMetadata.getPropertyNames().stream().forEach(propName -> {
				entry.getValue().computeIfAbsent(propName, key -> with(logger).trace(
						String.format("[%s#%s]: Masking property [%s]", entityType.getName(), entry.getKey(), propName),
						MASKER));
			});
		});
		aliasMap.values().forEach(propNames -> {
			entityMetadata.getPropertyNames().stream().forEach(propName -> {
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
		this.getters = entityMetadata.getGetters().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		if (producingFunctions.isEmpty()) {
			logger.debug(String.format("%s -> This %s is empty", entityType.getSimpleName(),
					this.getClass().getSimpleName()));
			return;
		}

		logger.debug(String.format("\n" + "%s:\n" + "\t%s", entityType.getName(),
				producingFunctions.entrySet().stream().map(entry -> entry.getValue().entrySet().stream()
						.map(fEntry -> String.format("%s:\t[%s] -> [%s]", entry.getKey(),
								this.aliasMap.get(entry.getKey()).get(fEntry.getKey()),
								fEntry.getValue().equals(MASKER) ? "masked"
										: fEntry.getValue().equals(PUBLISHER) ? "published" : fEntry.getValue()))
						.collect(Collectors.joining("\n\t"))).collect(Collectors.joining("\n\t"))));
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

	private Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> getFunctions(String evaluation)
			throws UnauthorizedCredential {
		Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> functions = producingFunctions
				.get(evaluation);

		if (functions == null) {
			throw new UnauthorizedCredential(evaluation);
		}

		return functions;
	}

	// @formatter:off
	private Map<String, Object> produceRow(
			String[] columns,
			Object[] values,
			Credential credential,
			Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> functions,
			Map<String, String> alias
			) {
		// @formatter:on
		try {
			int span = values.length;

			return IntStream.range(0, span).mapToObj(index -> {
				String column = columns[index];

				try {
					return entry(alias.get(column), functions.get(column).apply(argument(values[index]), credential));
				} catch (Exception e) {
					logger.debug(e.getMessage());

					return entry(alias.get(column), e.getMessage());
				}
			})
			// @formatter:off
			.collect(
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
	public Map<String, Object> produce(Object[] source, Credential credential) {
		return new HashMap<>();
	}

	@Override
	public List<Map<String, Object>> produce(List<Object[]> source, Credential credential) {
		return new ArrayList<>();
	}

	@Override
	public <E extends T> Map<String, Object> produceSingleSource(SingleSource<E> source, Credential credential)
			throws UnauthorizedCredential {
		SourceMetadata<E> metadata = source.getMetadata();
		String evaluation = credential.evaluate();
		Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> functions = getFunctions(evaluation);
		Map<String, String> alias = aliasMap.get(evaluation);

		if (!metadata.hasAssociation()) {
			return produceRow(metadata.getColumns(), source.getSource(), credential, functions, alias);
		}

		return produceRow(metadata.getColumns(), source.getSource(), credential,
				prepareAssociationProducing(metadata, functions), alias);
	}

	@Override
	public <E extends T> List<Map<String, Object>> produceBatchedSource(BatchedSource<E> source, Credential credential)
			throws UnauthorizedCredential {
		SourceMetadata<E> metadata = source.getMetadata();
		String evaluation = credential.evaluate();
		Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> functions = getFunctions(evaluation);
		Map<String, String> alias = aliasMap.get(evaluation);
		List<Object[]> batch = source.getSource();
		String[] columns = metadata.getColumns();

		if (!metadata.hasAssociation()) {
			// @formatter:off
			return IntStream.range(0, batch.size())
					.mapToObj(index -> produceRow(columns, batch.get(index), credential, functions, alias))
					.collect(Collectors.toList());
			// @formatter:on
		}

		Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> preparedFunctions = prepareAssociationProducing(
				metadata, functions);
		// @formatter:off
		return IntStream.range(0, batch.size())
				.mapToObj(index -> produceRow(columns, batch.get(index), credential, preparedFunctions, alias))
				.collect(Collectors.toList());
		// @formatter:on;
	}

	@Override
	public <E extends T> Map<String, Object> produceSinglePojo(SinglePojoSource<E> source, Credential credential)
			throws UnauthorizedCredential {
		SourceMetadata<E> metadata = source.getMetadata();
		String[] columns = metadata.getColumns();
		E entity = source.getSource();
		Object[] values = Stream.of(columns).map(column -> getters.get(column).get(entity)).toArray();

		return produceSingleSource(new SingleSourceImpl<>(metadata, values), credential);
	}

	@Override
	public <E extends T> List<Map<String, Object>> produceBatchedPojo(BatchedPojoSource<E> source,
			Credential credential) throws UnauthorizedCredential {
		SourceMetadata<E> metadata = source.getMetadata();
		String[] columns = metadata.getColumns();
		List<Object[]> batch = source.getSource().stream()
				.map(entity -> Stream.of(columns).map(column -> getters.get(column).get(entity)).toArray())
				.collect(Collectors.toList());

		return produceBatchedSource(new BatchedSourceImpl<>(metadata, batch), credential);
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

	// @formatter:off
	@SuppressWarnings("unchecked")
	private <E extends T> Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> prepareAssociationProducing(
			SourceMetadata<E> metadata,
			Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> configuredFunctions) {
		// @formatter:on
		String[] columns = metadata.getColumns();
		Set<Integer> associationIndices = metadata.getAssociationIndices();
		int span = columns.length;
		// we need this to be modifiable
		Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> copiedFunctions = new HashMap<>(
				configuredFunctions);

		IntStream.range(0, span).forEach(index -> {
			String column = columns[index];

			if (!associationIndices.contains(index)) {
				return;
			}

			SourceMetadata<DomainEntity> associationMetadata = (SourceMetadata<DomainEntity>) metadata
					.getAssociationMetadata(index);
			SourceType sourceType = associationMetadata.getSourceType();
			Class<DomainEntity> associationType = (Class<DomainEntity>) entityMetadata.getAssociationType(column);
			DynamicMapModelProducer<DomainEntity> associationProducer;

			if (sourceType == SourceType.OBJECT_ARRAY) {
				associationProducer = factory.getProducer(associationType);
				copiedFunctions.put(column,
						(arg, credential) -> associationProducer.produceSingleSource(
								new SingleSourceImpl<>(associationMetadata, optional(arg.getSource(), Object[].class)),
								credential));
				return;
			}

			if (sourceType == SourceType.POJO) {
				associationProducer = factory.getProducer(associationType);
				copiedFunctions.put(column, (arg, credential) -> associationProducer.produceSinglePojo(
						new SinglePojoSourceImpl<>(associationMetadata, optional(arg.getSource(), associationType)),
						credential));
				return;
			}

			if (sourceType == SourceType.COLLECTION) {
				associationProducer = factory.getProducer(associationType);

				Class<?> genericType = metadata.getRepresentation();
				// Object[]
				if (genericType.isArray()) {
					copiedFunctions.put(column,
							(arg, credential) -> associationProducer.produceBatchedSource(
									new BatchedSourceImpl<>(associationMetadata, optional(arg.getSource())),
									credential));
					return;
				}
				// POJO
				if (DomainEntity.class.isAssignableFrom(genericType)) {
					copiedFunctions.put(column,
							(arg, credential) -> associationProducer.produceBatchedPojo(
									new BatchedPojoSourceImpl<>(associationMetadata, optional(arg.getSource())),
									credential));
					return;
				}

				throw new IllegalArgumentException(String.format("Unknown association type %s", genericType));
			}

			throw new IllegalArgumentException(String.format("Unsupported association representation %s", sourceType));
		});

		return copiedFunctions;
	}

	@SuppressWarnings("unchecked")
	private <X> X optional(Object source, Class<X> type) {
		return source == null ? null : (X) source;
	}

	@SuppressWarnings("unchecked")
	private <X> ArrayList<X> optional(Object source) {
		return source == null ? null : new ArrayList<>((Collection<X>) source);
	}

}
