/**
 * 
 */
package adn.service.resource.engine;

import java.io.File;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import adn.helpers.ArrayHelper;
import adn.service.resource.engine.template.ResourceTemplate;
import adn.service.resource.engine.tuple.ResourceTuplizer;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Primary
public class FinderImpl implements Finder {

	private final Storage storage;
	private final String rootPath;

	private static final File[] NO_RESULTS = new File[0];

	@Autowired
	private FinderImpl(@Autowired Storage storage) {
		this.storage = storage;
		rootPath = storage.getDirectory();
	}

	@Override
	public File find(String path) {
		if (!path.startsWith(rootPath)) {
			return null;
		}

		File file = new File(path);

		return doesExist(file) ? file : null;
	}

	private String appendRootPath(String path) {
		return rootPath + path;
	}

	@Override
	public File find(String path, boolean appendRoot) {
		String checkedPath = path;

		if (!checkedPath.startsWith(rootPath)) {
			if (!appendRoot) {
				return null;
			}

			checkedPath = appendRootPath(path);
		}

		File file = new File(checkedPath);

		return doesExist(file) ? file : null;
	}

	private File find(ResourceTemplate template, String path) {
		return find(template.getDirectory() + path);
	}

	@Override
	public boolean doesExist(File file) {
		return file.exists() && file.isFile();
	}

	@Override
	public boolean doesExist(String path) {
		if (!path.startsWith(rootPath)) {
			return false;
		}

		return doesExist(new File(path));
	}

	@Override
	public File[] find(ResourceTemplate template, Object[] values) {
		return find(template, values, template.getColumnNames());
	}

	@Override
	public File[] find(ResourceTemplate template, Object[] conditions, String[] propertyNames) {
		File directory = new File(template.getDirectory());
		File[] allFiles = directory.listFiles();
		ResourceTuplizer tuplizer = template.getTuplizer();
		int span = propertyNames.length;
		ArrayHelper.ArrayBuilder<String> builder;

		if ((builder = ArrayHelper.from(propertyNames)).contains(template.getPathColumn())) {
			File fileByPath = find(template, (String) conditions[builder.getLastFoundIndex()]);

			return fileByPath == null ? NO_RESULTS : new File[] { fileByPath };
		}

		return Stream.of(allFiles).filter(file -> {
			if (!file.isFile()) {
				return false;
			}

			Object value;

			for (int i = 0; i < span; i++) {
				value = tuplizer.getPropertyValue(file, template.getColumnIndex(propertyNames[i]));

				if (!isSatisfied(value, conditions[i])) {
					return false;
				}
			}

			return true;
		}).toArray(File[]::new);
	}

	private boolean isSatisfied(Object one, Object two) {
		return one.equals(two);
	}

	public Storage getStorage() {
		return storage;
	}

}
