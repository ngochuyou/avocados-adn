/**
 * 
 */
package adn.service.resource.engine.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.hibernate.tuple.Tuplizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.service.resource.engine.Finder;
import adn.service.resource.engine.Storage;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.query.QueryCompiler;
import adn.service.resource.engine.template.ResourceTemplate;
import javassist.NotFoundException;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class PersistenceContext {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Storage storage;
	private final Finder finder;

	private final Map<String, Mutex> mutexMap = new MutexMap();

	@Autowired
	public PersistenceContext(@Autowired Storage storage, @Autowired Finder finder) {
		super();
		this.storage = storage;
		this.finder = finder;
	}

	private synchronized Mutex createMutex(File file, ResourceTemplate template) {
		Mutex newMutex = new Mutex();

		mutexMap.put(getFilenameOnly(file.getPath()), newMutex);

		return newMutex;
	}

	private String getFilenameOnly(String fileNameWithExtension) {
		return fileNameWithExtension.replaceAll("\\.[\\w\\d]+$", "");
	}

	public boolean update(Query query, ResourceTemplate template) throws NotFoundException {
		String[] setPortionColumnNames = getSetPortionParameterNames(query);
		String[] wherePortionColumnNames = getWherePortionParameterNames(query);
		Mutex mutex;
		ArrayList<Integer> setColumnIndicies = new ArrayList<>();

		for (String setColumnName : setPortionColumnNames) {
			if (template.getColumnIndex(setColumnName) == null) {
				setColumnIndicies.add(Optional.ofNullable(template.getColumnIndex(setColumnName))
						.orElseThrow(() -> new NotFoundException(
								String.format("Unknown column [%s] in SET portion", setColumnName))));
			}
		}

		File[] files = finder.find(query, wherePortionColumnNames, template,
				String.format("%s%s", QueryCompiler.WHERE_MARKER, template.getPathColumn()));
		Tuplizer tuplizer = template.getTuplizer();
		Object[] values;
		String mutexKey;
		File temp;
		logger.trace(String.format("Found {%d} file(s) for update", files.length));

		for (File file : files) {
			temp = new File(file.getPath());
			mutexKey = getFilenameOnly(temp.getPath());
			mutex = !mutexMap.containsKey(mutexKey)
					? createMutex(temp, storage.getResourceTemplate(query.getTemplateName()))
					: mutexMap.get(mutexKey);

			synchronized (mutex) {
				logger.trace(String.format("Locking file [%s] with mutex [%s]", temp.getPath(), mutexKey));

				values = resolveValues(query, template, setPortionColumnNames, temp);
				tuplizer.setPropertyValues(temp, values);
			}
		}

		return true;
	}

	private Object[] resolveValues(Query query, ResourceTemplate template, String[] providedColumns, File file) {
		Set<String> providedColumnSet = Set.of(providedColumns);
		String[] requiredColumns = template.getColumnNames();
		int n = requiredColumns.length;
		Object[] values = new Object[requiredColumns.length];
		String requiredColumn;
		Tuplizer tuplizer = template.getTuplizer();

		for (int i = 0; i < n; i++) {
			requiredColumn = requiredColumns[i];

			if (providedColumnSet.contains(requiredColumn)) {
				values[i] = query.getParameterValue(String.format("%s%s", QueryCompiler.SET_MARKER, requiredColumn));
				continue;
			}

			values[i] = tuplizer.getPropertyValue(file, i);
		}

		return values;
	}

	private String[] getSetPortionParameterNames(Query query) {
		// @formatter:off
		return query.getParameterNames()
				.stream().filter(paramName -> paramName.startsWith(QueryCompiler.SET_MARKER))
				.map(name -> name.replaceFirst(Pattern.quote(QueryCompiler.SET_MARKER), ""))
				.toArray(String[]::new);
		// @formatter:on
	}

	private String[] getWherePortionParameterNames(Query query) {
		// @formatter:off
		return query.getParameterNames()
				.stream().filter(paramName -> paramName.startsWith(Pattern.quote(QueryCompiler.WHERE_MARKER)))
				.map(name -> name.replaceFirst(Pattern.quote(QueryCompiler.WHERE_MARKER), ""))
				.toArray(String[]::new);
		// @formatter:on
	}

	@SuppressWarnings("unused")
	private class Mutex {

		private volatile boolean isLocked;

		Mutex() {}

		public void setLocked(boolean isLocked) {
			this.isLocked = isLocked;
		}

		public boolean isLocked() {
			return isLocked;
		}

	}

	class MutexMap extends LinkedHashMap<String, Mutex> {

		private static final long serialVersionUID = 1L;

		private static final int MAX_SIZE = 100;

		public MutexMap() {
			super(MAX_SIZE, 1f);
		}

		@Override
		protected boolean removeEldestEntry(java.util.Map.Entry<String, Mutex> eldest) {
			return size() > MAX_SIZE;
		}

	}

}
