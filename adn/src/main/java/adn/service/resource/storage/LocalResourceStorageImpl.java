/**
 * 
 */
package adn.service.resource.storage;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
@Component
public class LocalResourceStorageImpl implements LocalResourceStorage {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public LocalResourceStorageImpl() throws IllegalAccessException {
		// TODO Auto-generated constructor stub
		logger.trace("Registering supported Resource types returned by the " + LocalResourceStorage.class);
	}

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
	public ResultSetImplementor select(Serializable identifier) {
		// TODO Auto-generated method stub
		return new ResourceResultSet(
				Arrays.asList(validate(new File(LocalResourceStorage.IMAGE_FILE_DIRECTORY + identifier))));
	}

	@Override
	public ResultSetImplementor select(Serializable[] identifiers) {
		logger.debug("Selecting identifiers: "
				+ Stream.of(identifiers).map(id -> LocalResourceStorage.IMAGE_FILE_DIRECTORY + id.toString())
						.collect(Collectors.joining(", ")));
		// @formatter:off
		return new ResourceResultSet(Stream.of(identifiers)
				.map(id -> new File(LocalResourceStorage.IMAGE_FILE_DIRECTORY + id.toString()))
				.map(this::validate).collect(Collectors.toList()));
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
