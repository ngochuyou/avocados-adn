/**
 * 
 */
package adn.service.resource.engine;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;

import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.service.resource.engine.action.SaveAction;
import adn.service.resource.engine.action.SaveActionImpl;
import adn.service.resource.engine.persistence.PersistenceContext;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.template.ResourceTemplate;
import adn.service.resource.engine.template.ResourceTemplateImpl;
import adn.service.resource.engine.tuple.InstantiatorFactory.PojoInstantiator;
import adn.service.resource.engine.tuple.ResourceTuplizerImpl;
import javassist.NotFoundException;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class LocalStorage implements Storage {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Finder finder;

	private final Validator templateValidator;
	private final Map<String, ResourceTemplate> templates = new HashMap<>(8, .75f);
	private final MetadataFactory metadataFactory = MetadataFactory.INSTANCE;

	private final SaveAction saveAction = new SaveActionImpl(this);

	@Autowired
	private PersistenceContext persistenceContext;

	private static final String DIRECTORY = "C:\\Users\\Ngoc Huy\\Documents\\avocados-adn\\";

	// @formatter:off
	@Autowired
	private LocalStorage(
			@Autowired Validator templateValidator) {
		this.templateValidator = templateValidator;
	}
	// @formatter:on
	@Override
	public ResultSetImplementor query(Query query) {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("Executing query: [%s\n] ", query.toString()));
		}

		switch (query.getType()) {
			case SAVE: {
				return doSave(query);
			}
			case FIND: {
				return doFind(query);
			}
			case UPDATE: {
				return doUpdate(query);
			}
			default: {
				return new ExceptionResultSet(
						new RuntimeException(String.format("Unknown query [%s]", query.getActualSQLString())),
						query.getStatement());
			}
		}
	}

	private ResultSetImplementor doUpdate(Query batch) {
		ResourceTemplate template = templates.get(batch.getTemplateName());
		Query current = batch;
		long modCount = 0;

		while (current != null) {
			try {
				if (persistenceContext.update(current, template)) {
					modCount++;
					current = current.next();
				}
			} catch (NotFoundException nfe) {
				return new ExceptionResultSet(new SQLException(nfe), batch.getStatement());
			}
		}

		return new ResourceUpdateCount(new Long[] { modCount }, template.getTemplateName(), batch.getStatement());
	}

	private ResultSetImplementor doFind(Query query) {
		ResourceTemplate template = getResourceTemplate(query.getTemplateName());
		File file = finder.findByFileName(query, template, template.getPathColumn());

		if (file != null) {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Found one file with path [%s]", file.getPath()));
			}

			Object[] values;

			try {
				ResultSetMetadataImplementor metadata = metadataFactory.produce(query, template);

				values = extractValues(file, template, metadata);

				return new ResourceResultSet(new Object[][] { values }, metadata, query.getStatement());
			} catch (Exception any) {
				any.printStackTrace();
				return new ExceptionResultSet(new RuntimeException(any), query.getStatement());
			}
		}

		try {
			return new ResourceResultSet(new Object[0][0], metadataFactory.produce(query, template),
					query.getStatement());
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			return new ExceptionResultSet(new RuntimeException(sqle), query.getStatement());
		}
	}

	private Object[] extractValues(File file, ResourceTemplate template, ResultSetMetadataImplementor metadata) {
		Object[] values = ((ResourceTuplizerImpl) template.getTuplizer()).getPropertyValues(file,
				metadata.getActualColumnNames());

		return postValueExtractions(values, template, metadata);
	}

	private Object[] postValueExtractions(Object[] states, ResourceTemplate template,
			ResultSetMetadataImplementor metadata) {
		String filenameColumnName = template.getColumnNames()[0];

		if (Stream.of(metadata.getActualColumnNames()).filter(name -> name == filenameColumnName).count() == 1) {
			int i = metadata.getColumnIndexFromActualName(filenameColumnName);
			String path = String.valueOf(states[i]);

			states[i] = path.replaceFirst(LocalStorage.DIRECTORY + template.getDirectory(), "");
		}

		return states;
	}

	private ResultSetImplementor doSave(Query batch) {
		Query current = batch;
		long modCount = 0;

		while (current != null) {
			try {
				saveAction.execute(current);
				modCount++;
			} catch (RuntimeException rte) {
				rte.printStackTrace();
				return new ExceptionResultSet(new SQLException(rte), batch.getStatement());
			}

			current = current.next();
		}

		return new ResourceUpdateCount(new Long[] { modCount }, batch.getTemplateName(), batch.getStatement());
	}

	@Override
	public void registerTemplate(String templateName, String directoryName, String[] columnNames,
			Class<?>[] columnTypes, PropertyAccessImplementor[] accessors, PojoInstantiator<File> instantiator) throws IllegalArgumentException {
		if (templates.containsKey(templateName)) {
			throw new IllegalArgumentException(String.format("Duplicate template: [%s]", templateName));
		}

		validateAndPutTemplate(new ResourceTemplateImpl(templateName, directoryName, columnNames, columnTypes,
				accessors, instantiator, this));
	}

	private void validateAndPutTemplate(ResourceTemplate template) {
		templateValidator.validate(template, null);
		templates.put(template.getTemplateName(), template);
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

	@Override
	public String getDirectory() {
		return DIRECTORY;
	}

}
