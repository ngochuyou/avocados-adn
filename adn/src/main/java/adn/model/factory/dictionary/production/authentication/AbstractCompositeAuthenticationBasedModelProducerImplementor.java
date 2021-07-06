package adn.model.factory.dictionary.production.authentication;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import adn.model.AbstractModel;
import adn.model.entities.Entity;
import adn.model.factory.dictionary.production.AbstractCompositeDictionaryAuthenticationBasedModelProducer;
import adn.model.factory.dictionary.production.CompositeDictionaryAuthenticationBasedModelProducer;
import adn.service.internal.Role;

public abstract class AbstractCompositeAuthenticationBasedModelProducerImplementor<T extends AbstractModel>
		extends AbstractCompositeDictionaryAuthenticationBasedModelProducer<T>
		implements CompositeDictionaryAuthenticationBasedModelProducer<T> {

	protected final int propertySpan;

	private final Map<Role, Function<T, Map<String, Object>>> mappingProducers;
	private final Map<Role, BiFunction<T, Map<String, Object>, Map<String, Object>>> injectiveProducers;

	private static final Function<Entity, Map<String, Object>> DEFAULT_MAP_PRODUCER = new Function<>() {

		@Override
		public Map<String, Object> apply(Entity t) {
			return Collections.emptyMap();
		}

	};

	private static final BiFunction<Entity, Map<String, Object>, Map<String, Object>> DEFAULT_INJECTIVE_PRODUCER = new BiFunction<>() {
		@Override
		public Map<String, Object> apply(Entity t, Map<String, Object> model) {
			return model;
		}
	};

	private static final float LOAD_FACTOR = 1.075f;

	public AbstractCompositeAuthenticationBasedModelProducerImplementor() {
		this.propertySpan = 16;
		// @formatter:off
		Map<Role, Function<T, Map<String, Object>>> mapProducers = new HashMap<>();
		
		mapProducers.put(Role.ADMIN, this::produceForAdmin);
		mapProducers.put(Role.PERSONNEL, this::produceForPersonnel);
		mapProducers.put(Role.EMPLOYEE, this::produceForEmployee);
		mapProducers.put(Role.MANAGER, this::produceForManager);
		mapProducers.put(Role.CUSTOMER, this::produceForCustomer);

		registerDefaultMappingProducers(mapProducers);
		
		Map<Role, BiFunction<T, Map<String, Object>, Map<String, Object>>> injectiveProducers = new HashMap<>();
		
		injectiveProducers.put(Role.ADMIN, this::produceForAdmin);
		injectiveProducers.put(Role.PERSONNEL, this::produceForPersonnel);
		injectiveProducers.put(Role.EMPLOYEE, this::produceForEmployee);
		injectiveProducers.put(Role.MANAGER, this::produceForManager);
		injectiveProducers.put(Role.CUSTOMER, this::produceForCustomer);

		registerDefaultInjectiveProducers(injectiveProducers);
		// @formatter:on
		this.mappingProducers = Collections.unmodifiableMap(mapProducers);
		this.injectiveProducers = Collections.unmodifiableMap(injectiveProducers);
	}

	@SuppressWarnings("unchecked")
	private void registerDefaultMappingProducers(Map<Role, Function<T, Map<String, Object>>> mapProducers) {
		for (Role role : Role.values()) {
			if (!mapProducers.containsKey(role)) {
				mapProducers.put(role, (Function<T, Map<String, Object>>) DEFAULT_MAP_PRODUCER);
			}
		}

		mapProducers.put(null, (Function<T, Map<String, Object>>) DEFAULT_MAP_PRODUCER);
	}

	@SuppressWarnings("unchecked")
	private void registerDefaultInjectiveProducers(
			Map<Role, BiFunction<T, Map<String, Object>, Map<String, Object>>> injectiveProducers) {
		for (Role role : Role.values()) {
			if (!injectiveProducers.containsKey(role)) {
				injectiveProducers.put(role,
						(BiFunction<T, Map<String, Object>, Map<String, Object>>) DEFAULT_INJECTIVE_PRODUCER);
			}
		}

		injectiveProducers.put(null,
				(BiFunction<T, Map<String, Object>, Map<String, Object>>) DEFAULT_INJECTIVE_PRODUCER);
	}

	@Override
	public final Map<String, Object> produce(T entity, Role role) {
		return mappingProducers.get(role).apply(entity);
	}

	@Override
	public final Map<String, Object> produceImmutable(T entity, Role role) {
		return Collections.unmodifiableMap(produce(entity, role));
	}

	@Override
	public final Map<String, Object> produce(T entity, Map<String, Object> modelMap, Role role) {
		return injectiveProducers.get(role).apply(entity, modelMap);
	}

	@Override
	public final Map<String, Object> produceImmutable(T entity, Map<String, Object> modelMap, Role role) {
		return Collections.unmodifiableMap(produce(entity, modelMap, role));
	}

	@Override
	public final Map<String, Object> produce(T source) {
		return mappingProducers.get(null).apply(source);
	}

	@Override
	public final Map<String, Object> produceImmutable(T source) {
		return Collections.unmodifiableMap(produce(source));
	}

	@Override
	public final Map<String, Object> produce(T source, Map<String, Object> model) {
		return injectiveProducers.get(null).apply(source, model);
	}

	@Override
	public final Map<String, Object> produceImmutable(T source, Map<String, Object> model) {
		return Collections.unmodifiableMap(produce(source, model));
	}

	@Override
	public List<Map<String, Object>> produce(List<T> sources) {
		Function<T, Map<String, Object>> producer = mappingProducers.get(null);

		return IntStream.range(0, sources.size()).mapToObj(index -> producer.apply(sources.get(index)))
				.collect(Collectors.toList());
	}

	@Override
	public List<Map<String, Object>> produceImmutable(List<T> sources) {
		return Collections.unmodifiableList(produce(sources));
	}

	@Override
	public List<Map<String, Object>> produce(List<T> source, Role role) {
		Function<T, Map<String, Object>> producer = mappingProducers.get(role);

		return IntStream.range(0, source.size()).mapToObj(index -> producer.apply(source.get(index)))
				.collect(Collectors.toList());
	}

	@Override
	public List<Map<String, Object>> produceImmutable(List<T> source, Role role) {
		return Collections.unmodifiableList(produce(source, role));
	}

	@Override
	public List<Map<String, Object>> produce(List<T> source, List<Map<String, Object>> models) {
		return produce(source, models, null);
	}

	@Override
	public List<Map<String, Object>> produceImmutable(List<T> source, List<Map<String, Object>> models) {
		return Collections.unmodifiableList(produce(source, models));
	}

	@Override
	public List<Map<String, Object>> produce(List<T> source, List<Map<String, Object>> models, Role role) {
		BiFunction<T, Map<String, Object>, Map<String, Object>> producer = injectiveProducers.get(role);

		return IntStream.range(0, source.size()).mapToObj(index -> producer.apply(source.get(index), models.get(index)))
				.collect(Collectors.toList());
	}

	@Override
	public List<Map<String, Object>> produceImmutable(List<T> source, List<Map<String, Object>> models, Role role) {
		return Collections.unmodifiableList(produce(source, models, role));
	}

	protected Map<String, Object> createModel() {
		return new HashMap<>(propertySpan, LOAD_FACTOR);
	}

	protected final Map<String, Object> produceForAdmin(T entity) {
		return produceForAdmin(entity, createModel());
	}

	protected final Map<String, Object> produceForPersonnel(T entity) {
		return produceForPersonnel(entity, createModel());
	}

	protected final Map<String, Object> produceForEmployee(T entity) {
		return produceForEmployee(entity, createModel());
	}

	protected final Map<String, Object> produceForManager(T entity) {
		return produceForManager(entity, createModel());
	}

	protected final Map<String, Object> produceForCustomer(T entity) {
		return produceForCustomer(entity, createModel());
	}

	protected final Map<String, Object> produceForAnonymous(T entity) {
		return produceForAnonymous(entity, createModel());
	}

	// -----defaults are inject nothing, mask all----- //

	protected Map<String, Object> produceForAdmin(T entity, Map<String, Object> model) {
		return model;
	}

	protected Map<String, Object> produceForPersonnel(T entity, Map<String, Object> model) {
		return model;
	}

	protected Map<String, Object> produceForEmployee(T entity, Map<String, Object> model) {
		return model;
	}

	protected Map<String, Object> produceForManager(T entity, Map<String, Object> model) {
		return model;
	}

	protected Map<String, Object> produceForCustomer(T entity, Map<String, Object> model) {
		return model;
	}

	protected Map<String, Object> produceForAnonymous(T entity, Map<String, Object> model) {
		return model;
	}

}
