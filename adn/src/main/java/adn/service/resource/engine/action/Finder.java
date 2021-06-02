/**
 * 
 */
package adn.service.resource.engine.action;

import java.io.File;

import org.springframework.util.Assert;

import adn.application.Constants;
import adn.helpers.StringHelper;
import adn.service.resource.engine.LocalStorage;
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
		ResourceTemplate template = storage.getResourceTemplate(query.getTemplateName());

		Assert.notNull(template, String.format("Unable to locate %s from template name [%s]", ResourceTemplate.class,
				query.getTemplateName()));

		return find(query, template);
	}

	private String toPath(String filename, String... additionalDirectory) {
		return String.format("%s%s%s", Constants.LOCAL_STORAGE_DIRECTORY, StringHelper.get(additionalDirectory[0], ""),
				filename);
	}

}
