package adn.model.factory.production.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import adn.model.AbstractModel;
import adn.model.entities.Entity;
import adn.model.factory.AbstractCompositeAuthenticationBasedModelProducer;
import adn.model.factory.CompositeAuthenticationBasedModelProducer;
import adn.service.internal.Role;

public abstract class AbstractCompositeAuthenticationBasedModelProducerImplementor<T extends AbstractModel> extends
		AbstractCompositeAuthenticationBasedModelProducer<T> implements CompositeAuthenticationBasedModelProducer<T> {

	protected final int propertySpan;

	private final Map<Role, Function<T, Map<String, Object>>> mapProducers;
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

		registerDefaultMapProducers(mapProducers);
		
		Map<Role, BiFunction<T, Map<String, Object>, Map<String, Object>>> injectiveProducers = new HashMap<>();
		
		injectiveProducers.put(Role.ADMIN, this::produceForAdmin);
		injectiveProducers.put(Role.PERSONNEL, this::produceForPersonnel);
		injectiveProducers.put(Role.EMPLOYEE, this::produceForEmployee);
		injectiveProducers.put(Role.MANAGER, this::produceForManager);
		injectiveProducers.put(Role.CUSTOMER, this::produceForCustomer);

		registerDefaultInjectiveProducers(injectiveProducers);
		// @formatter:on
		this.mapProducers = Collections.unmodifiableMap(mapProducers);
		this.injectiveProducers = Collections.unmodifiableMap(injectiveProducers);
	}

	@SuppressWarnings("unchecked")
	private void registerDefaultMapProducers(Map<Role, Function<T, Map<String, Object>>> mapProducers) {
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
		return mapProducers.get(role).apply(entity);
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
