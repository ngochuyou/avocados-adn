/**
 * 
 */
package adn.service.resource.engine.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import adn.service.resource.engine.LocalStorage;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.template.ResourceTemplate;

/**
 * @author Ngoc Huy
 *
 */
public class SaveActionImpl implements SaveAction {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final LocalStorage storage;

	private final Map<Class<?>, Function<Object, byte[]>> contentExtractors = Map.of(byte[].class,
			(bytes) -> (byte[]) bytes);

	public SaveActionImpl(LocalStorage storage) {
		this.storage = storage;
	}

	@Override
	public void execute(Query query) throws RuntimeException {
		ResourceTemplate template = storage.getResourceTemplate(query.getTemplateName());

		Assert.notNull(template, String.format("Unable to locate %s for name [%s]", ResourceTemplate.class.getName(),
				query.getTemplateName()));

		String contentColumnName;

		if ((contentColumnName = template.getColumnNames()[1]).equals(ResourceTemplate.NO_CONTENT.toString())) {
			logger.trace(String.format("Ignoring save action on unsavable template[%s]", template.getName()));
			return;
		}

		File instance = storage.instantiate(query, template);
		// @formatter:off
		template.getTuplizer().setPropertyValues(
				instance,
				Stream.of(template.getColumnNames())
						.map(columnName -> query.getParameterValue(columnName))
						.toArray(Object[]::new));
		// @formatter:on
		checkDuplicate(instance);

		Class<?> contentType = template.getColumnTypes()[1];

		if (!contentExtractors.containsKey(contentType)) {
			throw new IllegalArgumentException(
					String.format("Unable to extract content out of type [%s] from instance of template [%s]",
							contentType, template.getName()));
		}

		try {
			byte[] content = contentExtractors.get(template.getColumnTypes()[1])
					.apply(query.getParameterValue(contentColumnName));

			logger.trace(String.format("Saving [%s], content length [%s]", instance.getPath(), content.length));
			Files.write(Paths.get(instance.getPath()), content);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	private void checkDuplicate(File file) {
		if (file.exists()) {
			throw new IllegalArgumentException(String.format("Duplicate entry [%s]", file.getPath()));
		}
	}

	@Override
	public LocalStorage getStorage() {
		return storage;
	}

}
