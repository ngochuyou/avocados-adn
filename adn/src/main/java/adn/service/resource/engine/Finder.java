/**
 * 
 */
package adn.service.resource.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.hibernate.tuple.Tuplizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.helpers.ArrayHelper;
import adn.helpers.StringHelper;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.template.ResourceTemplate;
import javassist.NotFoundException;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class Finder {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Storage storage;

	@Autowired
	private Finder(@Autowired Storage storage) {
		this.storage = storage;
	}

	public File findByFileName(Query query, ResourceTemplate template, String pathColumnName) {
		Object pathValue = query.getParameterValue(pathColumnName);
		File file = new File(toPath(pathValue.toString(), template.getDirectory()));

		return file.exists() ? file : null;
	}

	public File[] find(Query query, String[] columnNames, ResourceTemplate template, String... pathColumnNames)
			throws NotFoundException {
		String pathColumnName = pathColumnNames.length == 0 ? template.getPathColumn() : pathColumnNames[0];

		if (query.getParameterNames().contains(pathColumnName)) {
			return filter(template, query, ArrayHelper.from(columnNames).remove(pathColumnName).get(),
					new File[] { findByFileName(query, template, pathColumnName) });
		}

		return filter(template, query, columnNames, obtainFromDirectory(template.getDirectory()));
	}

	private File[] obtainFromDirectory(String directory) {
		File file = new File(storage.getDirectory() + directory);

		if (file.exists() && file.isDirectory()) {
			return Stream.of(file.listFiles()).filter(File::isFile).toArray(File[]::new);
		}

		return new File[0];
	}

	private File[] filter(ResourceTemplate template, Query query, String[] columnNames, File[] files)
			throws NotFoundException {
		ArrayList<File> filteredFiles = new ArrayList<>(files.length);
		List<Integer> columnIndicies = new ArrayList<>();

		for (String columnName : columnNames) {
			columnIndicies.add(Optional.of(template.getColumnIndex(columnName)).orElseThrow(
					() -> new NotFoundException(String.format("Unknown column [%s] in WHERE portion", columnName))));
		}

		int n = columnNames.length;
		Tuplizer tuplizer = template.getTuplizer();
		boolean match;

		for (File file : files) {
			match = true;

			for (int i = 0; i < n; i++) {
				if (logger.isTraceEnabled()) {
					logger.trace(String.format("[%s] -> tuplizer:[%s]|query:[%s]", columnNames[i],
							tuplizer.getPropertyValue(file, columnIndicies.get(i)),
							query.getParameterValue(columnNames[i])));
				}

				if (!tuplizer.getPropertyValue(file, columnIndicies.get(i))
						.equals(query.getParameterValue(columnNames[i]))) {
					match = false;
					break;
				}
			}

			if (match) {
				filteredFiles.add(file);
			}
		}

		filteredFiles.trimToSize();

		return filteredFiles.toArray(File[]::new);
	}

	public File findByFileName(Query query) {
		ResourceTemplate template = storage.getResourceTemplate(query.getTemplateName());

		return findByFileName(query, template, template.getPathColumn());
	}

	private String toPath(String filename, String... additionalDirectory) {
		return String.format("%s%s%s", storage.getDirectory(), StringHelper.get(additionalDirectory[0], ""), filename);
	}

}
