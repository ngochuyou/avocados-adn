/**
 * 
 */
package adn.service.resource.engine;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;

import adn.helpers.ArrayHelper;
import adn.helpers.ArrayHelper.ArrayBuilder;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.service.resource.engine.action.SaveAction;
import adn.service.resource.engine.action.SaveActionImpl;
import adn.service.resource.engine.persistence.PersistenceContext;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.template.ResourceTemplate;
import adn.service.resource.engine.template.ResourceTemplateImpl;
import adn.service.resource.engine.tuple.InstantiatorFactory.PojoInstantiator;
import adn.service.resource.engine.tuple.ResourceTuplizer;
import javassist.NotFoundException;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class LocalStorage implements Storage {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Qualifier("default_finder")
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

	private static final String PATHNAME_GROUPNAME = "pathname";
	private static final Pattern PATHNAME_PATTERN;

	static {
		String path = "[\\w\\d_-]+(\\\\)?";
		PATHNAME_PATTERN = Pattern.compile(String.format("^(?<dir>(%s)+)?(?<%s>(%s)+)(?<extension>\\.[\\w\\d]+)$",
				Pattern.quote("[\\w\\d]+:\\") + path, PATHNAME_GROUPNAME, path));
	}

	private static final String FILENAME_TRIMMING_STRING = String.format("${%s}", PATHNAME_GROUPNAME);

	/**
	 * If the path column of a {@link File} is being extracted, get rid of the
	 * extension and the directory path if they present
	 * </p>
	 * Then, we order the columns so that they respect the columns order in the
	 * {@link Query}
	 * 
	 */
	private Object[] doPostValueExtractions(ResultSetMetadataImplementor metadata, ResourceTemplate template,
			Object[] extractedValues, String[] columnsToExtract) {
		ArrayBuilder<String> builder = ArrayHelper.from(columnsToExtract);

		if (!builder.contains(template.getPathColumn())) {
			return extractedValues;
		}

		int pathIndex = builder.getLastFoundIndex();
		String extractedPath = (String) extractedValues[pathIndex];
		final Matcher m = PATHNAME_PATTERN.matcher(extractedPath);

		if (m.matches()) {
			extractedPath = m.replaceAll(FILENAME_TRIMMING_STRING);
		}

		extractedValues[pathIndex] = extractedPath;
		// @formatter:off
		return Stream.of(metadata.getActualColumnNames())
				.map(requestedColumn -> extractedValues[template.getColumnIndex(requestedColumn)])
				.toArray();
		// @formatter:on
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
