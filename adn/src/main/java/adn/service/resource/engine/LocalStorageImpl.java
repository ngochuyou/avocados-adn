/**
 * 
 */
package adn.service.resource.engine;

import java.io.File;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.validation.Validator;

import adn.application.Constants;
import adn.service.resource.engine.action.SaveAction;
import adn.service.resource.engine.action.SaveActionImpl;
import adn.service.resource.engine.query.Query;
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
	private final MetadataFactory metadataFactory = MetadataFactory.INSTANCE;

	private final SaveAction saveAction = new SaveActionImpl(this);
	private final Finder finder = new Finder(this);

	@Autowired
	private LocalStorageImpl() {}

	@Override
	public ResultSetImplementor query(Query query) {
		logger.trace(String.format("Executing query: [%s\n] ", query.toString()));

		switch (query.getType()) {
			case SAVE: {
				return doSave(query);
			}
			case FIND: {
				return doFind(query);
			}
			default: {
				return new ExceptionResultSet(
						new RuntimeException(String.format("Unknown query type [%s]", query.getType())));
			}
		}
	}

	private ResultSetImplementor doFind(Query query) {
		ResourceTemplate template = getResourceTemplate(query.getTemplateName());
		File file = finder.find(query, template);

		if (file != null) {
			logger.trace(String.format("Found one file with path [%s]", file.getPath()));

			Object[] values;

			try {
				ResultSetMetadataImplementor metadata = metadataFactory.produce(query, template);

				values = extractValues(file, template, metadata);

				return new ResourceResultSet(new Object[][] { values }, metadata);
			} catch (Exception any) {
				any.printStackTrace();
				return new ExceptionResultSet(new RuntimeException(any));
			}
		}

		try {
			return new ResourceResultSet(new Object[0][0], metadataFactory.produce(query, template));
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			return new ExceptionResultSet(new RuntimeException(sqle));
		}
	}

	private Object[] extractValues(File file, ResourceTemplate template, ResultSetMetadataImplementor metadata) {
		Object[] values = ((ResourceTuplizer) template.getTuplizer()).getPropertyValues(file,
				metadata.getActualColumnNames());

		return postValueExtractions(values, template, metadata);
	}

	private Object[] postValueExtractions(Object[] states, ResourceTemplate template,
			ResultSetMetadataImplementor metadata) {
		String filenameColumnName = template.getColumnNames()[0];

		if (Stream.of(metadata.getActualColumnNames()).filter(name -> name == filenameColumnName).count() == 1) {
			int i = metadata.getColumnIndexFromActualName(filenameColumnName);
			String path = String.valueOf(states[i]);

			states[i] = path.replaceFirst(Constants.LOCAL_STORAGE_DIRECTORY + template.getDirectoryName(), "");
		}

		return states;
	}

	private ResultSetImplementor doSave(Query query) {
		Query current = query;
		List<Integer> results = new ArrayList<>();

		while (current != null) {
			try {
				saveAction.execute(current);
				results.add(1);
			} catch (Exception e) {
				results.add(0);
			}

			current = current.next();
		}

		return new ResourceUpdateCount(results.toArray(Integer[]::new), query.getTemplateName());
	}

	public File instantiate(Query query, ResourceTemplate template) {
		PojoInstantiator<File> instantiator = template.getInstantiator();

		if (instantiator instanceof ParameterizedInstantiator) {
			ParameterizedInstantiator<File> inst = (ParameterizedInstantiator<File>) instantiator;
			String[] parameterNames = inst.getParameterNames();
			Class<?>[] parameterTypes = inst.getParameterTypes();
			Object[] parameters = Stream.of(parameterNames).map(paramName -> query.getParameterValue(paramName))
					.toArray(Object[]::new);

			preInstantiate(parameterTypes, parameters, template.getDirectoryName());

			return inst.instantiate(parameters);
		}

		return instantiator.instantiate();
	}

	private void preInstantiate(Class<?>[] parameterTypes, Object[] parameters, String... additionalDirectory)
			throws IllegalArgumentException {
		if (parameterTypes[0].equals(String.class)) {
			// new File(String [,String])
			Assert.isTrue(parameters[0] instanceof String,
					String.format("Parameter type mismatch at index [0], [%s><%s]",
							parameters[0] != null ? parameters[0].getClass() : null, String.class));
			parameters[0] = Constants.LOCAL_STORAGE_DIRECTORY
					+ Stream.of(additionalDirectory).collect(Collectors.joining()) + parameters[0];
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

	@Override
	public ResourceTemplate getResourceTemplate(String templateName) {
		return templates.get(templateName);
	}

	@Override
	public ResultSetImplementor execute(Query query) throws RuntimeException {
		return null;
	}

}
