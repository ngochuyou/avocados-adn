/**
 * 
 */
package adn.service.resource.engine;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import adn.helpers.StringHelper;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.template.ResourceTemplate;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class LocalStorageImpl implements LocalStorage {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final ResultSetMetaDataImplementor metadata = ResultSetMetaDataImpl.INSTANCE;
	private final Map<String, ResourceTemplate> templates = new HashMap<>(8, .75f);

	@Autowired
	public LocalStorageImpl() {}

	public boolean isExists(String filename) {
		// TODO Auto-generated method stub
		File file = obtainImage(filename);

		return file.exists() && !file.isDirectory();
	}

	private File obtainImage(String filename) {

		return new File(LocalStorage.IMAGE_FILE_DIRECTORY + filename);
	}

	@Override
	public ResultSetImplementor select(Serializable identifier) {
		return new ResourceResultSet(Arrays.asList(validate(new File(LocalStorage.IMAGE_FILE_DIRECTORY + identifier))));
	}

	@Override
	public ResultSetImplementor select(Serializable[] identifiers) {
		logger.debug("Selecting identifiers: " + Stream.of(identifiers)
				.map(id -> LocalStorage.IMAGE_FILE_DIRECTORY + id.toString()).collect(Collectors.joining(", ")));
		// @formatter:off
		return new ResourceResultSet(Stream.of(identifiers)
				.map(id -> new File(LocalStorage.IMAGE_FILE_DIRECTORY + id.toString()))
				.map(this::validate).collect(Collectors.toList()));
		// @formatter:on
	}

	private File validate(File file) {
		return file.exists() && !file.isDirectory() ? file : null;
	}

	@Override
	public void lock(Serializable identifier) {}

	@Override
	public ResultSetImplementor query(Query query) {
		logger.trace(String.format("Executing query: [%s] ", query.toString()));

		return null;
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
		logger.trace(String.format("Registered new resource template: [\n%s\n]", template.toString()));
	}

	private void validateTemplate(ResourceTemplate template) throws IllegalArgumentException {
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
