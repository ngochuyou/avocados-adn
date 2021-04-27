/**
 * 
 */
package adn.service.resource.storage;

import java.io.File;
import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
@Component
public class LocalResourceStorageImpl implements LocalResourceStorage {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public boolean isExists(String filename) {
		// TODO Auto-generated method stub
		File file = obtainImage(filename);

		return file.exists() && !file.isDirectory();
	}

	private File obtainImage(String filename) {

		return new File(LocalResourceStorage.IMAGE_FILE_DIRECTORY + filename);
	}

	@Override
	public SingleResourceResultSet select(Serializable identifier) {
		// TODO Auto-generated method stub
		return new SingleResourceResultSet(validate(new File(LocalResourceStorage.IMAGE_FILE_DIRECTORY + identifier)),
				File.class);
	}

	@Override
	public ResourceResultSet select(Serializable[] identifiers) {
		// @formatter:off
		logger.debug("Selecting identifiers: " + Stream.of(identifiers).map(id -> LocalResourceStorage.IMAGE_FILE_DIRECTORY + id.toString()).collect(Collectors.joining(", ")));
		return new ResourceResultSet(Stream.of(identifiers)
				.map(id -> new File(LocalResourceStorage.IMAGE_FILE_DIRECTORY + id.toString()))
				.map(this::validate).collect(Collectors.toList()), File.class);
		// @formatter:on
	}

	private File validate(File file) {
		return file.exists() && !file.isDirectory() ? file : null;
	}

	@Override
	public void lock(Serializable identifier) {
		// TODO Auto-generated method stub

	}

}
