/**
 * 
 */
package adn.service.resource.engine;

import java.io.File;

import org.springframework.util.Assert;

import adn.application.Constants;
import adn.helpers.StringHelper;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.template.ResourceTemplate;

/**
 * @author Ngoc Huy
 *
 */
public class Finder {

	private final LocalStorage storage;

	public Finder(LocalStorage storage) {
		this.storage = storage;
	}

	public File find(Query query, ResourceTemplate template) {
		String pathColumnName = template.getColumnNames()[0];
		Object pathValue = query.getParameterValue(pathColumnName);
		String path = "";

		Assert.isTrue(pathValue != null && StringHelper.hasLength(path = pathValue.toString()),
				String.format("[%s] was not given in the query [%s]", pathColumnName, query.toString()));

		File file = new File(toPath(path, template.getDirectoryName()));

		return file.exists() ? file : null;
	}

	public File find(Query query) {
		return find(query, storage.getResourceTemplate(query.getTemplateName()));
	}

	private String toPath(String filename, String... additionalDirectory) {
		return String.format("%s%s%s", Constants.LOCAL_STORAGE_DIRECTORY, StringHelper.get(additionalDirectory[0], ""),
				filename);
	}

}
