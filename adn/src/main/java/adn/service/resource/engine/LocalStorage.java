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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;

import adn.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.service.resource.engine.persistence.PersistenceContext;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.query.UpdateQuery;
import adn.service.resource.engine.template.ResourceTemplate;
import adn.service.resource.engine.template.ResourceTemplateImpl;
import adn.service.resource.engine.tuple.InstantiatorFactory.PojoInstantiator;
import adn.service.resource.engine.tuple.ResourceTuplizer;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class LocalStorage implements Storage {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Qualifier(FinderImpl.NAME)
	private Finder finder;

	private final Validator templateValidator;
	private final Map<String, ResourceTemplate> templates = new HashMap<>(8, .75f);
	private final MetadataFactory metadataFactory = MetadataFactory.INSTANCE;

	@Autowired
	private PersistenceContext persistenceContext;

	public static final String DIRECTORY = "C:\\Users\\Ngoc Huy\\Documents\\avocados-adn\\";
	public static final int MAX_SIZE_IN_ONE_READ = 5 * 1024 * 1024; // 5MB
	
	// @formatter:off
	@Autowired
	private LocalStorage(
			@Autowired Validator templateValidator) {
		this.templateValidator = templateValidator;
	}
	// @formatter:on
	@Override
	public ResultSetImplementor query(Query query) {
		logger.info(String.format("Executing query: [%s\n] ", query.toString()));

		switch (query.getType()) {
			case SAVE: {
				return doSave(query);
			}
			case FIND: {
				return doFind(query);
			}
			case UPDATE: {
				return doUpdate((UpdateQuery) query);
			}
			default: {
				return new ExceptionResultSet(
						new RuntimeException(String.format("Unknown query [%s]", query.getActualSQLString())),
						query.getStatement());
			}
		}
	}

	private ResultSetImplementor doUpdate(UpdateQuery batch) {
		ResourceTemplate template = templates.get(batch.getTemplateName());
		UpdateQuery current = batch;
		long modCount = 0;
		SQLException error = null;
		boolean success = true;

		while (current != null) {
			success = persistenceContext.update(current, template, error);

			if (success) {
				modCount++;
				current = current.next();
				continue;
			}

			if (error != null) {
				error.printStackTrace();
			}

			return new ExceptionResultSet(error, current.getStatement());
		}

		return new ResourceUpdateCount(new Long[] { modCount }, template.getTemplateName(), batch.getStatement());
	}

	private ResultSetImplementor doFind(Query query) {
		ResourceTemplate template = getResourceTemplate(query.getTemplateName());
		String[] queriedColumns = query.getColumnNames();

		for (String column : queriedColumns) {
			if (template.getColumnIndex(column) == null) {
				return new ExceptionResultSet(new IllegalArgumentException(
						String.format("Unknown column [%s] in template [%s]", column, template.getTemplateName())),
						query.getStatement());
			}
		}

		Object[] values = new Object[queriedColumns.length];
		int span = queriedColumns.length;

		for (int i = 0; i < span; i++) {
			values[i] = query.getParameterValue(queriedColumns[i]);
		}

		template.getTuplizer().validate(values, queriedColumns);

		File[] fileList = finder.find(template, values, queriedColumns);

		try {
			ResultSetMetadataImplementor metadata = metadataFactory.produce(query, template);
			Object[][] rows = toRows(metadata, template, fileList, queriedColumns);

			return new ResourceResultSet(rows, metadata, query.getStatement());
		} catch (Exception any) {
			any.printStackTrace();
			return new ExceptionResultSet(new RuntimeException(any), query.getStatement());
		}
	}

	private Object[][] toRows(ResultSetMetadataImplementor metadata, ResourceTemplate template, File[] fileList,
			String[] queriedColumns) {
		int filesAmount = fileList.length;
		Object[][] results = new Object[filesAmount][queriedColumns.length];

		if (logger.isTraceEnabled()) {
			logger.trace(String.format("Producing {%d} row(s) for ResultSet", results.length));
		}

		for (int i = 0; i < filesAmount; i++) {
			results[i] = extractValues(metadata, template, fileList[i], template.getColumnNames());
		}

		return results;
	}

	private Object[] extractValues(ResultSetMetadataImplementor metadata, ResourceTemplate template, File file,
			String[] columnsToExtract) {
		int span = columnsToExtract.length;
		Object[] values = new Object[span];
		ResourceTuplizer tuplizer = template.getTuplizer();

		for (int i = 0; i < span; i++) {
			values[i] = tuplizer.getPropertyValue(file, i);
		}

		return doPostValueExtractions(metadata, template, values, columnsToExtract);
	}

	/**
	 * Order the columns so that they respect the columns order in the {@link Query}
	 */
	private Object[] doPostValueExtractions(ResultSetMetadataImplementor metadata, ResourceTemplate template,
			Object[] extractedValues, String[] columnsToExtract) {
		// @formatter:off
		return Stream.of(metadata.getActualColumnNames())
				.map(requestedColumn -> extractedValues[template.getColumnIndex(requestedColumn)])
				.toArray();
		// @formatter:on
	}

	private ResultSetImplementor doSave(Query batch) {
		Query current = batch;
		long modCount = 0;
		SQLException error = null;
		boolean success;

		try {
			while (current != null) {
				success = persistenceContext.save(current, getResourceTemplate(current.getTemplateName()), error);

				if (success) {
					modCount++;
					current = current.next();
					continue;
				}

				if (error != null) {
					error.printStackTrace();
				}

				return new ExceptionResultSet(error, batch.getStatement());
			}
		} catch (RuntimeException rte) {
			rte.printStackTrace();
			return new ExceptionResultSet(rte, batch.getStatement());
		}

		return new ResourceUpdateCount(new Long[] { modCount }, batch.getTemplateName(), batch.getStatement());
	}

	@Override
	public void registerTemplate(String templateName, String directoryName, String[] columnNames,
			Class<?>[] columnTypes, boolean[] columnNullabilities, PropertyAccessImplementor[] accessors,
			PojoInstantiator<File> instantiator) throws IllegalArgumentException {
		if (templates.containsKey(templateName)) {
			throw new IllegalArgumentException(String.format("Duplicate template: [%s]", templateName));
		}
		// @formatter:off
		validateAndPutTemplate(new ResourceTemplateImpl(
				templateName,
				directoryName,
				columnNames,
				columnTypes,
				columnNullabilities,
				accessors,
				instantiator,
				this));
		// @formatter:on
	}

	private void validateAndPutTemplate(ResourceTemplate template) {
		templateValidator.validate(template, null);
		templates.put(template.getTemplateName(), template);
		logger.info(String.format("Registered new resource template: [\n%s\n]", template.toString()));
	}

	@Override
	public ResourceTemplate getResourceTemplate(String templateName) {
		return templates.get(templateName);
	}

	@Override
	public ResultSetImplementor execute(Query query) throws RuntimeException {
		logger.info(String.format("Executing query: [%s\n] ", query.toString()));

		switch (query.getType()) {
			case SAVE: {
				return doSave(query);
			}
			case UPDATE: {
				return doUpdate((UpdateQuery) query);
			}
			default: {
				return new ExceptionResultSet(
						new RuntimeException(String.format("Unknown query [%s]", query.getActualSQLString())),
						query.getStatement());
			}
		}
	}

	@Override
	public String getDirectory() {
		return DIRECTORY;
	}

}
