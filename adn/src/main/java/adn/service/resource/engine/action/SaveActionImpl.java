/**
 * 
 */
package adn.service.resource.engine.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import adn.service.resource.engine.Storage;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.template.ResourceTemplate;

/**
 * @author Ngoc Huy
 *
 */
public class SaveActionImpl implements SaveAction {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Storage storage;

	public SaveActionImpl(Storage storage) {
		this.storage = storage;
	}

	@Override
	public void execute(Query query) throws RuntimeException {
		ResourceTemplate template = storage.getResourceTemplate(query.getTemplateName());

		Assert.notNull(template, String.format("Unable to locate %s for name [%s]", ResourceTemplate.class.getName(),
				query.getTemplateName()));

		File instance = null;

		checkDuplicate(instance);
		// @formatter:off
		template.getTuplizer().setPropertyValues(
				instance,
				Stream.of(template.getColumnNames())
						.map(columnName -> query.getParameterValue(columnName))
						.toArray(Object[]::new));
		// @formatter:on
	}

	private void checkDuplicate(File file) {
//		if (storage.doesExist(file)) {
//			throw new IllegalArgumentException(String.format("Duplicate entry [%s]", file.getPath()));
//		}
	}

	@Override
	public Storage getStorage() {
		return storage;
	}

	@Override
	public boolean performContentSave(File file, byte[] content) throws RuntimeException {
		try {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Saving [%s], content length [%s]", file.getPath(), content.length));
			}

			Files.write(Paths.get(file.getPath()), content);
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}

		return false;
	}

}
