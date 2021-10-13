/**
 * 
 */
package adn.model.factory.authentication.dynamicmap;

import static adn.helpers.LoggerHelper.with;
import static adn.helpers.Utils.Entry.entry;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

import adn.helpers.CollectionHelper;
import adn.helpers.FunctionHelper.HandledBiFunction;
import adn.helpers.StringHelper;
import adn.helpers.TypeHelper;
import adn.helpers.Utils;
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

	private final Class<T> entityType;
	private static final float LOAD_FACTOR = 1.175f;

	private final Map<String, Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>>> producingFunctions;
	private final Map<String, String> aliasMap;
	private final Map<String, String> namesMap;
	private final Map<String, Getter> getters;

	private final DynamicMapModelProducerFactory factory;
	private final DomainEntityMetadata<T> entityMetadata;
	private final String[] nonLazyProperties;

	/**
	 * 
	 */
	public DynamicMapModelProducerImpl(Class<T> entityType, Set<SecuredProperty<T>> properties,
			DomainEntityMetadata<T> entityMetadata, DynamicMapModelProducerFactory factory) {
		this.entityType = entityType;
		this.factory = factory;
		this.entityMetadata = entityMetadata;

		Map<String, Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>>> producingFunctions = new HashMap<>(
				0);
		Map<String, String> aliasMap = new HashMap<>(0);

		if (logger.isTraceEnabled()) {
			logger.trace(String.format("Following %s were passed into this %s<%s>\n\t%s",
					SecuredProperty.class.getName(), DynamicMapModelProducerImpl.class.getSimpleName(),
					entityType.getSimpleName(), properties.stream().map(securedProp -> securedProp.toString())
							.collect(Collectors.joining("\n\t"))));
		}

		// @formatter:off
		properties.stream().filter(prop -> {
			boolean isParent = TypeHelper.isParentOf(prop.getOwningType(), entityType);
			boolean unknownProperty = !entityMetadata.hasProperty(prop.getName());
			boolean nullFunction = prop.getFunction() == null;
			
			if (!isParent || unknownProperty || nullFunction) {
				if (logger.isTraceEnabled()) {
					logger.trace(String.format("[%s]: Ignoring %s [%s] %s", entityType.getName(),
							SecuredProperty.class.getSimpleName(), prop.getName(),
							!isParent ? "invalid type"
									: unknownProperty ? "unknown property"
											: " null function"));
				}
				
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

			if (function == null) {
				throw new IllegalArgumentException(String.format("%s[%s] function was empty on property [%s]", entityType.getSimpleName(), credential, name));
			}

			if (!producingFunctions.containsKey(credential)) {
				producingFunctions.put(credential,
						Stream.of(Map.entry(name, resolveFormatters(function, prop, entityMetadata)))
							.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
				useAlias(aliasMap, name, alias);
				return;
			}
			
			Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> functions = producingFunctions.get(credential);

			if (functions.containsKey(name)) {
				if (functions.get(name) != function) {
					HandledBiFunction<Arguments<?>, Credential, ?, Exception> oldFnc = functions.put(name, resolveFormatters(function, prop, entityMetadata));
					
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("[%s#%s] Overriding function of property [%s] using [%s], overridden function was [%s]",
								entityType, credential, name, function, oldFnc));
					}
				}

				useAlias(aliasMap, name, alias);
				return;
			}
			
			functions.put(name, resolveFormatters(function, prop, entityMetadata));
			useAlias(aliasMap, name, alias);
		});
		// @formatter:on
		producingFunctions.entrySet().forEach(entry -> {
			entityMetadata.getPropertyNames().stream().forEach(propName -> {
				entry.getValue().computeIfAbsent(propName, key -> with(logger).trace(
						String.format("[%s#%s]: Masking property [%s]", entityType.getName(), entry.getKey(), propName),
						MASKER));
			});
		});
		entityMetadata.getPropertyNames().stream().forEach(propName -> {
			aliasMap.computeIfAbsent(propName, key -> propName);
		});

		this.producingFunctions = producingFunctions;
		this.aliasMap = Collections.unmodifiableMap(aliasMap);
		this.namesMap = Collections.unmodifiableMap(
				aliasMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)));
		this.getters = entityMetadata.getGetters();
		this.nonLazyProperties = entityMetadata.getNonLazyPropertyNames().toArray(String[]::new);

		if (producingFunctions.isEmpty()) {
			logger.debug(String.format("%s -> This %s is empty", entityType.getSimpleName(),
					this.getClass().getSimpleName()));
			return;
		}

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("\n" + "%s:\n" + "\t%s", entityType.getName(),
					producingFunctions.entrySet().stream().map(entry -> entry.getValue().entrySet().stream()
							.map(fEntry -> String.format("%s:\t[%s as %s] -> [%s]", entry.getKey(), fEntry.getKey(),
									this.aliasMap.get(fEntry.getKey()),
									fEntry.getValue().equals(MASKER) ? "masked"
											: fEntry.getValue().equals(PUBLISHER) ? "published" : fEntry.getValue()))
							.collect(Collectors.joining("\n\t"))).collect(Collectors.joining("\n\t"))));
		}
	}

	private void useAlias(Map<String, String> aliasMap, String name, String alias) {
		if (!StringHelper.hasLength(alias)) {
			logger.trace("Ignorig empty alias");
			return;
		}

		String oldAlias = aliasMap.get(alias);

		if (oldAlias != null && !oldAlias.equals(alias)) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Overriding alternative name using [%s], overridden name was [%s]", alias,
						oldAlias));
			}
		}

		aliasMap.put(name, alias);
	}

	private HandledBiFunction<Arguments<?>, Credential, ?, Exception> resolveFormatters(
			HandledBiFunction<Arguments<?>, Credential, ?, Exception> function, SecuredProperty<T> property,
			DomainEntityMetadata<T> metadata) {
		if (function != PUBLISHER) {
			return function;
		}

		Class<?> propertyType = metadata.getPropertyType(property.getName());

		if (LocalDateTime.class.isAssignableFrom(propertyType)) {
			return LOCALDATETIME_SOURCE_FORMATER;
		}

		if (LocalDate.class.isAssignableFrom(propertyType)) {
			return LOCALDATE_SOURCE_FORMATER;
		}

		return function;
	}

	@Override
	public List<String> validateColumns(Credential credential, Collection<String> columns)
			throws NoSuchFieldException, UnauthorizedCredential {
		String evaluation = credential.evaluate();

		assertCredential(evaluation);

		List<String> translatedColumnNames = new ArrayList<>(columns.size());
		String translatedColumn;

		for (String requestedColumn : columns) {
			if ((translatedColumn = namesMap.get(requestedColumn)) == null) {
				throw new NoSuchFieldException(String.format("Unknown property [%s]", requestedColumn));
			}

			translatedColumnNames.add(translatedColumn);
		}

		return translatedColumnNames;
	}

	private void assertCredential(String evaluation) throws UnauthorizedCredential {
		if (!producingFunctions.containsKey(evaluation)) {
			throw new UnauthorizedCredential(evaluation, entityType.getSimpleName());
		}
	}

	// @formatter:off
	private Map<String, Object> produceRow(
			String[] columns,
			Object[] values,
			Credential credential,
			Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> functions
			) {
		// @formatter:on
		try {
			int span = values.length;

			return IntStream.range(0, span).mapToObj(index -> {
				String column = columns[index];

				try {
					return entry(aliasMap.get(column),
							functions.get(column).apply(argument(values[index]), credential));
				} catch (Exception e) {
					logger.error(String.format("Error occured producing columns [%s] on entity [%s]", column,
							entityType.getName()));
					e.printStackTrace();

					return entry(aliasMap.get(column), null);
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
	public <E extends T> Map<String, Object> produceSingleSource(SingleSource<E> sourceArgument, Credential credential)
			throws UnauthorizedCredential {
		Object[] source = sourceArgument.getSource();

		if (source == null) {
			return null;
		}

		SourceMetadata<E> metadata = sourceArgument.getMetadata();
		String evaluation = credential.evaluate();
		Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> functions = producingFunctions
				.get(evaluation);
		String[] columns = hasLengthOrNonLazy(metadata.getColumns());

		if (!metadata.hasAssociation()) {
			return produceRow(columns, source, credential, functions);
		}

		return produceRow(columns, source, credential, prepareAssociationProducing(metadata, functions));
	}

	@Override
	public <E extends T> List<Map<String, Object>> produceBatchedSource(BatchedSource<E> source, Credential credential)
			throws UnauthorizedCredential {
		List<Object[]> batch = source.getSource();

		if (batch == null) {
			return new ArrayList<>();
		}

		SourceMetadata<E> metadata = source.getMetadata();
		String evaluation = credential.evaluate();
		Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> functions = producingFunctions
				.get(evaluation);
		String[] columns = hasLengthOrNonLazy(metadata.getColumns());

		if (!metadata.hasAssociation()) {
			// @formatter:off
			return IntStream.range(0, batch.size())
					.mapToObj(index -> produceRow(columns, batch.get(index), credential, functions))
					.collect(Collectors.toList());
			// @formatter:on
		}

		Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> preparedFunctions = prepareAssociationProducing(
				metadata, functions);
		// @formatter:off
		return IntStream.range(0, batch.size())
				.mapToObj(index -> produceRow(columns, batch.get(index), credential, preparedFunctions))
				.collect(Collectors.toList());
		// @formatter:on;
	}

	private String[] hasLengthOrNonLazy(List<String> possibleEmpty) {
		return CollectionHelper.isEmpty(possibleEmpty) ? nonLazyProperties : possibleEmpty.toArray(String[]::new);
	}

	@Override
	public <E extends T> Map<String, Object> produceSinglePojo(SinglePojoSource<E> sourceArgument,
			Credential credential) throws UnauthorizedCredential {
		E source = sourceArgument.getSource();

		if (source == null) {
			return null;
		}

		SourceMetadata<E> metadata = sourceArgument.getMetadata();
		String evaluation = credential.evaluate();
		Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> functions = producingFunctions
				.get(evaluation);
		String[] columns = hasLengthOrNonLazy(metadata.getColumns());
		E entity = sourceArgument.getSource();
		Object[] values = Stream.of(columns).map(column -> getters.get(column).get(entity)).toArray();

		if (!metadata.hasAssociation()) {
			return produceRow(columns, values, credential, functions);
		}

		Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> preparedFunctions = prepareAssociationProducing(
				metadata, functions);

		return produceRow(columns, values, credential, preparedFunctions);
	}

	@Override
	public <E extends T> List<Map<String, Object>> produceBatchedPojo(BatchedPojoSource<E> source,
			Credential credential) throws UnauthorizedCredential {
		List<E> batch = source.getSource();

		if (CollectionHelper.isEmpty(batch)) {
			return new ArrayList<>();
		}

		SourceMetadata<E> metadata = source.getMetadata();
		String evaluation = credential.evaluate();
		Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> functions = producingFunctions
				.get(evaluation);
		String[] columns = hasLengthOrNonLazy(metadata.getColumns());

		if (!metadata.hasAssociation()) {
			return IntStream.range(0, batch.size())
					.mapToObj(index -> produceRow(columns,
							Stream.of(columns).map(column -> getters.get(column).get(batch.get(index))).toArray(),
							credential, functions))
					.collect(Collectors.toList());
		}

		Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> preparedFunctions = prepareAssociationProducing(
				metadata, functions);

		return IntStream.range(0, batch.size())
				.mapToObj(index -> produceRow(columns,
						Stream.of(columns).map(column -> getters.get(column).get(batch.get(index))).toArray(),
						credential, preparedFunctions))
				.collect(Collectors.toList());
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
		List<String> columns = metadata.getColumns();
		Set<Integer> associationIndices = metadata.getAssociationIndices();
		int span = columns.size();
		// we need this to be modifiable
		Map<String, HandledBiFunction<Arguments<?>, Credential, ?, Exception>> copiedFunctions = new HashMap<>(
				configuredFunctions);

		IntStream.range(0, span).forEach(index -> {
			String column = columns.get(index);

			if (!associationIndices.contains(index)) {
				return;
			}

			SourceMetadata<DomainEntity> associationMetadata = (SourceMetadata<DomainEntity>) metadata
					.getAssociationMetadata(index);
			SourceType sourceType = associationMetadata.getSourceType();
			Class<DomainEntity> associationType = (Class<DomainEntity>) entityMetadata.getAssociationClass(column);
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
				// TODO: do not remove
//				Class<?> genericType = metadata.getRepresentation();

				// Object[]
				// This is temporarily 'unused' since it's use cases are very rare,
				// at least until Hibernate 6
//				if (genericType.isArray()) {
//					copiedFunctions.put(column,
//							(arg, credential) -> associationProducer.produceBatchedSource(
//									new BatchedSourceImpl<>(associationMetadata, optional(arg.getSource())),
//									credential));
//					return;
//				}
				// That being said, this is the only case
				// POJO
//				if (DomainEntity.class.isAssignableFrom(genericType)) {
				copiedFunctions.put(column,
						(arg, credential) -> associationProducer.produceBatchedPojo(
								new BatchedPojoSourceImpl<>(associationMetadata, optional(arg.getSource())),
								credential));
				return;
//				}

//				throw new IllegalArgumentException(String.format("Unknown association type %s", genericType));
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

	private static final HandledBiFunction<Arguments<?>, Credential, ?, Exception> LOCALDATETIME_SOURCE_FORMATER = new HandledBiFunction<>() {

		@Override
		public Object apply(Arguments<?> arg, Credential credential) throws Exception {
			Object value = arg.getSource();

			return value == null ? null : Utils.localDateTime((LocalDateTime) value);
		}

	};
	private static final HandledBiFunction<Arguments<?>, Credential, ?, Exception> LOCALDATE_SOURCE_FORMATER = new HandledBiFunction<>() {

		@Override
		public Object apply(Arguments<?> arg, Credential credential) throws Exception {
			Object value = arg.getSource();

			return value == null ? null : Utils.localDate((LocalDate) value);
		}

	};

}
