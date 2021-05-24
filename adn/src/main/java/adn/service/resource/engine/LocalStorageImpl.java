/**
 * 
 */
package adn.service.resource.engine;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.hibernate.property.access.spi.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import adn.helpers.StringHelper;
import adn.helpers.TypeHelper;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
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

	private final Map<String, ResourceTemplate> templates = new HashMap<>(8, .75f);
	private final Map<String, ResultSetMetaDataImpl> metadataMap = new HashMap<>(8, .75f);
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
		ResourceTemplate template = templates.get(query.getTemplateName());
		String[] columnNames = template.getColumnNames();
		PropertyAccessImplementor[] accessors = template.getPropertyAccessors();
		int span = template.getColumnNames().length;
		Object instance = instantiate(query, template);

		for (int i = 0; i < span; i++) {
			injectValue(accessors[i], columnNames[i], query, instance);
		}

		checkDuplicate(instance);

		return new ResourceResultSet(Arrays.asList(), metadataMap.get(template.getName()));
	}

	private void checkDuplicate(Object instance) throws SQLException {
		if (DUPLICATE_CHECKERS.get(instance.getClass()).apply(instance)) {
			throw new SQLException(String.format("Duplicate entry [%s]", instance));
		}
	}

	private File instantiate(Query query, ResourceTemplate template) {
		ResourceInstantiator<File> instantiator = template.getInstantiator();

		if (instantiator instanceof ParameterizedInstantiator) {
			ParameterizedInstantiator<File> inst = (ParameterizedInstantiator<File>) instantiator;
			String[] parameterNames = inst.getParameterNames();

			return inst.instantiate(Stream.of(parameterNames).map(paramName -> query.getParameterValue(paramName))
					.toArray(Object[]::new));
		}

		return instantiator.instantiate();
	}

	@SuppressWarnings("unchecked")
	private <T, E extends Throwable> void injectValue(PropertyAccessImplementor access, String name, Query query,
			T systemInstance) throws SQLException {
		try {
//			if (access instanceof FunctionalPropertyAccess) {
//				FunctionalPropertyAccess<T, ?, ?> accessFnc = (FunctionalPropertyAccess<T, ?, ?>) access;
//
//				if (accessFnc.hasSetterFunction()) {
//					accessFnc.getSetterFunction().apply(systemInstance);
//				}
//
//				return;
//			}

//			if (access.hasSetter()) {
//				checkTypeAndSet(systemInstance, access.getSetter(), query.getParameterValue(name));
//			}
		} catch (Throwable e) {
			throw new SQLException(e);
		}
	}

	private <T> void checkTypeAndSet(T instance, Setter setter, Object value) throws SQLException {
		Class<?> paramType = setter.getMethod().getParameterTypes()[0];

		if (!paramType.equals(value.getClass())) {
			if (!TypeHelper.TYPE_CONVERTER.containsKey(paramType)) {
				throw new SQLException(String.format("Type mismatch [%s><%s]", paramType, value.getClass()));
			}

			logger.trace(String.format("Casting [%s] -> [%s]", value.getClass(), paramType));
			setter.set(instance, TypeHelper.TYPE_CONVERTER.get(paramType).get(value.getClass()).apply(value), null);
			return;
		}

		setter.set(instance, value, null);
	}

	@Override
	public void registerTemplate(ResourceTemplate template) throws IllegalArgumentException {
		if (templates.containsKey(template.getName())) {
			throw new IllegalArgumentException(String.format("Duplicate template: [%s]", template.getName()));
		}

		validateAndPutTemplate(template);
	}

	private void validateAndPutTemplate(ResourceTemplate template) {
		validateTemplate(template);
		templates.put(template.getName(), template);
		metadataMap.put(template.getName(), new ResultSetMetaDataImpl(template.getName(), template.getColumnNames()));
		logger.trace(String.format("Registered new resource template: [\n%s\n]", template.toString()));
	}

	private void validateTemplate(ResourceTemplate template) throws IllegalArgumentException {
		logger.trace(String.format("Validating template: [%s]", template.getName()));

		Assert.isTrue(StringHelper.hasLength(template.getName()), "Template name must not be empty");
		Assert.isTrue(StringHelper.hasLength(template.getPathColumnName()), "Unable to locate pathname column");
		Assert.notNull(template.getColumnNames(),
				String.format("[%s]: Resource column names must not be null", template.getName()));

		int span = template.getColumnNames().length;

		Assert.isTrue(span == template.getColumnTypes().length, "Column names span and column types span must match");
		Assert.isTrue(span == template.getPropertyAccessors().length,
				"Column names span and property accessors span must match");
		Assert.notNull(template.getPropertyAccessors()[0],
				String.format("Property access of column [%s] must not be null", template.getColumnNames()[0]));

		for (int i = 1; i < span; i++) {
			Assert.isTrue(StringHelper.hasLength(template.getColumnNames()[i]),
					String.format("Column name must not be empty, found null at index [%d]", i));
			Assert.notNull(template.getColumnTypes()[i],
					String.format("Column type must not be null, found null at index [%d]", i));
			Assert.notNull(template.getPropertyAccessors()[i],
					String.format("Property access of column [%s] must not be null", template.getColumnNames()[i]));
		}
	}

}
