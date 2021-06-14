/**
 * 
 */
package adn.service.resource.engine.persistence;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.hibernate.tuple.Tuplizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.service.resource.engine.FinderImpl;
import adn.service.resource.engine.Storage;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.query.QueryCompiler;
import adn.service.resource.engine.template.ResourceTemplate;
import adn.service.resource.engine.tuple.ResourceTuplizer;
import javassist.NotFoundException;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class PersistenceContext {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Storage storage;
	private final FinderImpl finder;

	private final Map<String, Mutex> mutexMap = new MutexMap();

	@Autowired
	public PersistenceContext(@Autowired Storage storage, @Autowired FinderImpl finder) {
		super();
		this.storage = storage;
		this.finder = finder;
	}

	private Mutex createMutex(File file) {
		Mutex newMutex = new Mutex();

		mutexMap.put(file.getPath(), newMutex);

		return newMutex;
	}

	private String getFilenameOnly(String fileNameWithExtension) {
		return fileNameWithExtension.replaceAll("\\.[\\w\\d]+$", "");
	}

	private Mutex obtainMutex(File file) {
		if (mutexMap.containsKey(file.getPath())) {
			return mutexMap.get(file.getPath());
		}

		return createMutex(file);
	}

	public boolean save(Query query, ResourceTemplate template, SQLException error) {
		String[] registeredColumnNames = template.getColumnNames();
		Object[] values = Stream.of(registeredColumnNames).map(query::getParameterValue).toArray(Object[]::new);
		ResourceTuplizer tuplizer = template.getTuplizer();

		tuplizer.validate(values, registeredColumnNames);

		File newFile = tuplizer.instantiate(values);

		if (finder.doesExist(newFile)) {
			error = new SQLException(String.format("Duplicate filename [%s]", newFile.getName()));
			return false;
		}

		synchronized (obtainMutex(newFile)) {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Saving a new file with path [%s]", newFile.getPath()));
			}

			try {
				tuplizer.setPropertyValues(newFile, values);
			} catch (Exception any) {
				error = new SQLException(any);
				return false;
			}
		}

		return true;
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

		File[] files = finder.find(template, null);
		Tuplizer tuplizer = template.getTuplizer();
		Object[] values;
		String mutexKey;
		File temp;
		logger.trace(String.format("Found {%d} file(s) for update", files.length));

		for (File file : files) {
			temp = new File(file.getPath());
			mutexKey = getFilenameOnly(temp.getPath());
			mutex = !mutexMap.containsKey(mutexKey) ? createMutex(temp) : mutexMap.get(mutexKey);

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
		return Stream.of(query.getColumnNames())
				.filter(paramName -> paramName.startsWith(QueryCompiler.SET_MARKER))
				.map(name -> name.replaceFirst(Pattern.quote(QueryCompiler.SET_MARKER), ""))
				.toArray(String[]::new);
		// @formatter:on
	}

	private String[] getWherePortionParameterNames(Query query) {
		// @formatter:off
		return Stream.of(query.getColumnNames())
				.filter(paramName -> paramName.startsWith(Pattern.quote(QueryCompiler.WHERE_MARKER)))
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
			return size() >= MAX_SIZE;
		}

	}

}
