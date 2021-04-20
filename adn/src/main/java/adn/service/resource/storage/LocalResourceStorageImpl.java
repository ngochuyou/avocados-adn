/**
 * 
 */
package adn.service.resource.storage;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
@Component
public class LocalResourceStorageImpl implements LocalResourceStorage {

	public static final String LOCAL_DIRECTORY = "C:\\Users\\Ngoc Huy\\Documents\\avocados-adn\\";

	public static final String IMAGE_FILE_DIRECTORY = LOCAL_DIRECTORY + "images\\";

	public static final String DEFAULT_USER_PHOTO_NAME = "aad81c87bd8316705c4568e72577eb62476a.jpg";

	@Override
	public boolean isExists(String filename) {
		// TODO Auto-generated method stub
		File file = obtainImage(filename);

		return file.exists() && !file.isDirectory();
	}

	private File obtainImage(String filename) {

		return new File(IMAGE_FILE_DIRECTORY + filename);
	}

	@Override
	public <T> T select(Serializable identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> select(Serializable[] identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void lock(Serializable identifier) {
		// TODO Auto-generated method stub

	}

}
