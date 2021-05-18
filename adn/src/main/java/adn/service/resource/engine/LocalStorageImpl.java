/**
 * 
 */
package adn.service.resource.engine;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.hibernate.property.access.spi.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import adn.helpers.StringHelper;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.FunctionalPropertyAccess;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessDelegate;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.query.QueryCompiler.QueryType;
import adn.service.resource.engine.template.ResourceTemplate;
import adn.service.resource.engine.tuple.InstantiatorFactory.ParameterizedInstantiator;
import adn.service.resource.engine.tuple.InstantiatorFactory.ResourceInstantiator;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class LocalStorageImpl implements LocalStorage {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<String, ResourceTemplate<?>> templates = new HashMap<>(8, .75f);
	private final Map<String, ResultSetMetaDataImpl> metadataMap = new HashMap<>(8, .75f);
	// @formatter:off
	public static final Map<Class<?>, Map<Class<?>, Function<Object, Object>>> resolvers = Map.of(
			Timestamp.class, Map.of(
					Long.class, (longVal) -> new Timestamp((Long) longVal),
					Date.class, (date) -> new Timestamp(((Date) date).getTime())
			),
			Date.class, Map.of(
					Long.class, (longVal) -> new Date((Long) longVal)
			),
			long.class, Map.of(
					Timestamp.class, (stamp) -> ((Timestamp) stamp).getTime()
			)
	);
	
	private final Map<Class<?>, Consumer<Object>> SAVE_EXECUTORS = Map.of(
		File.class, (file) -> {
			try {
				((File) file).createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	);
	
	private final Map<Class<?>, Function<Object, Boolean>> DUPLICATE_CHECKERS = Map.of(
		File.class, (file) -> {
			try {
				return ((File) file).exists();
			} catch (Exception e) {
				e.printStackTrace();
				return true;
			}
		}
	);
	// @formatter:on
	@Autowired
	public LocalStorageImpl() {}

	@Override
	public ResultSetImplementor query(Query query) throws SQLException {
		logger.trace(String.format("Executing query: [%s] ", query.toString()));

		if (query.getType() == QueryType.SAVE) {
			return doSave(query);
		}

		throw new SQLException(String.format("Unknown query type [%s]", query.getType()));
	}

	private ResultSetImplementor doSave(Query query) throws SQLException {
		ResourceTemplate<?> template = templates.get(query.getTemplateName());
		String[] columnNames = template.getColumnNames();
		PropertyAccessDelegate[] accessors = template.getPropertyAccessors();
		int span = template.getColumnNames().length;
		Object instance = instantiate(query, template);

		for (int i = 0; i < span; i++) {
			injectValue(accessors[i], columnNames[i], query, instance);
		}

		checkDuplicate(instance);
		SAVE_EXECUTORS.get(template.getSystemType()).accept(instance);

		return new ResourceResultSet(Arrays.asList(), metadataMap.get(template.getName()));
	}

	private void checkDuplicate(Object instance) throws SQLException {
		if (DUPLICATE_CHECKERS.get(instance.getClass()).apply(instance)) {
			throw new SQLException(String.format("Duplicate entry [%s]", instance));
		}
	}

	private <T> T instantiate(Query query, ResourceTemplate<T> template) {
		ResourceInstantiator<T> instantiator = template.getInstantiator();

		if (instantiator instanceof ParameterizedInstantiator) {
			ParameterizedInstantiator<T> inst = (ParameterizedInstantiator<T>) instantiator;
			String[] parameterNames = inst.getParameterNames();

			return inst.instantiate(Stream.of(parameterNames).map(paramName -> query.getParameterValue(paramName))
					.toArray(Object[]::new));
		}

		return instantiator.instantiate();
	}

	@SuppressWarnings("unchecked")
	private <T, E extends Throwable> void injectValue(PropertyAccessDelegate access, String name, Query query,
			T systemInstance) throws SQLException {
		try {
			if (access instanceof FunctionalPropertyAccess) {
				FunctionalPropertyAccess<T, ?, ?> accessFnc = (FunctionalPropertyAccess<T, ?, ?>) access;

				if (accessFnc.hasSetterFunction()) {
					accessFnc.getSetterFunction().apply(systemInstance);
				}

				return;
			}

			if (access.hasSetter()) {
				checkTypeAndSet(systemInstance, access.getSetter(), query.getParameterValue(name));
			}
		} catch (Throwable e) {
			throw new SQLException(e);
		}
	}

	private <T> void checkTypeAndSet(T instance, Setter setter, Object value) throws SQLException {
		Class<?> paramType = setter.getMethod().getParameterTypes()[0];

		if (!paramType.equals(value.getClass())) {
			if (!resolvers.containsKey(paramType)) {
				throw new SQLException(String.format("Type mismatch [%s><%s]", paramType, value.getClass()));
			}

			logger.trace(String.format("Casting [%s] -> [%s]", value.getClass(), paramType));
			setter.set(instance, resolvers.get(paramType).get(value.getClass()).apply(value), null);
			return;
		}

		setter.set(instance, value, null);
	}

	@Override
	public void registerTemplate(ResourceTemplate<?> template) throws IllegalArgumentException {
		if (templates.containsKey(template.getName())) {
			throw new IllegalArgumentException(String.format("Duplicate template: [%s]", template.getName()));
		}

		validateAndPutTemplate(template);
	}

	private void validateAndPutTemplate(ResourceTemplate<?> template) {
		validateTemplate(template);
		templates.put(template.getName(), template);
		metadataMap.put(template.getName(), new ResultSetMetaDataImpl(template.getName(), template.getColumnNames()));
		logger.trace(String.format("Registered new resource template: [\n%s\n]", template.toString()));
	}

	private void validateTemplate(ResourceTemplate<?> template) throws IllegalArgumentException {
		logger.trace(String.format("Validating template: [%s]", template.getName()));

		Assert.isTrue(StringHelper.hasLength(template.getName()), "Template name must not be empty");
		Assert.notNull(template.getSystemType(), "Template system type must not be null");

		int span = template.getColumnNames().length;

		Assert.isTrue(span == template.getColumnTypes().length, "Column names span and column types span must match");
		Assert.isTrue(span == template.getPropertyAccessors().length,
				"Column names span and property accessors span must match");

		for (int i = 0; i < span; i++) {
			Assert.isTrue(StringHelper.hasLength(template.getColumnNames()[i]),
					String.format("Column name must not be empty, found null at index [%s]", i));
			Assert.notNull(template.getPropertyAccessors()[i],
					String.format("Property access of column [%s] must not be null", template.getColumnNames()[i]));
		}
	}

}
