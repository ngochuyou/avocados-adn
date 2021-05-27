/**
 * 
 */
package adn.service.resource.engine;

import java.io.File;
import java.net.URI;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.validation.Validator;

import adn.application.Constants;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.query.QueryCompiler.QueryType;
import adn.service.resource.engine.template.ResourceTemplate;
import adn.service.resource.engine.tuple.InstantiatorFactory.ParameterizedInstantiator;
import adn.service.resource.engine.tuple.InstantiatorFactory.PojoInstantiator;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class LocalStorageImpl implements LocalStorage {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Validator templateValidator;
	private final Map<String, ResourceTemplate> templates = new HashMap<>(8, .75f);

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
		File instance = instantiate(query, template);
		// @formatter:off
		template.getTuplizer().setPropertyValues(
				instance,
				Stream.of(template.getColumnNames())
						.map(columnName -> query.getParameterValue(columnName))
						.toArray(Object[]::new));
		// @formatter:on
		checkDuplicate(instance);

		return new ResourceResultSet(Arrays.asList(), template.getResultSetMetaData());
	}

	private void checkDuplicate(File file) throws SQLException {
		if (file.exists()) {
			throw new SQLException(String.format("Duplicate entry [%s]", file.getPath()));
		}
	}

	private File instantiate(Query query, ResourceTemplate template) {
		PojoInstantiator<File> instantiator = template.getInstantiator();

		if (instantiator instanceof ParameterizedInstantiator) {
			ParameterizedInstantiator<File> inst = (ParameterizedInstantiator<File>) instantiator;
			String[] parameterNames = inst.getParameterNames();
			Class<?>[] parameterTypes = inst.getParameterTypes();
			Object[] parameters = Stream.of(parameterNames).map(paramName -> query.getParameterValue(paramName))
					.toArray(Object[]::new);

			preInstantiate(parameterTypes, parameters);

			return inst.instantiate(parameters);
		}

		return instantiator.instantiate();
	}

	private void preInstantiate(Class<?>[] parameterTypes, Object[] parameters) throws IllegalArgumentException {
		if (parameterTypes[0].equals(String.class)) {
			// new File(String [,String])
			Assert.isTrue(parameters[0] instanceof String,
					String.format("Parameter type mismatch at index [0], [%s><%s]",
							parameters[0] != null ? parameters[0].getClass() : null, String.class));
			parameters[0] = Constants.LOCAL_STORAGE_DIRECTORY + parameters[0];
			return;
		}

		if (parameterTypes[0].equals(File.class)) {
			// new File(File, String)
			Assert.isTrue(parameters[0] instanceof String,
					String.format("Parameter type mismatch at index [0], [%s><%s]",
							parameters[0] != null ? parameters[0].getClass() : null, File.class));

			String pathname = ((File) parameters[0]).getPath();

			Assert.isTrue(pathname.startsWith(Constants.LOCAL_STORAGE_DIRECTORY),
					String.format("Invalid pathname [%s]", pathname));
			return;
		}
		// new File(URI)
		Assert.isTrue(parameters[0] instanceof String, String.format("Parameter type mismatch at index [0], [%s><%s]",
				parameters[0] != null ? parameters[0].getClass() : null, URI.class));

		String pathname = ((URI) parameters[0]).getRawPath();

		Assert.isTrue(pathname.startsWith(Constants.LOCAL_STORAGE_DIRECTORY),
				String.format("Invalid pathname [%s]", pathname));
	}

	@Override
	public void registerTemplate(ResourceTemplate template) throws IllegalArgumentException {
		if (templates.containsKey(template.getName())) {
			throw new IllegalArgumentException(String.format("Duplicate template: [%s]", template.getName()));
		}

		validateAndPutTemplate(template);
	}

	private void validateAndPutTemplate(ResourceTemplate template) {
		templateValidator.validate(template, null);
		templates.put(template.getName(), template);
		logger.trace(String.format("Registered new resource template: [\n%s\n]", template.toString()));
	}

}
