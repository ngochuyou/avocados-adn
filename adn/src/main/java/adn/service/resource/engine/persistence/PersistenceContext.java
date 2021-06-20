/**
 * 
 */
package adn.service.resource.engine.persistence;

import java.io.File;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import adn.helpers.ArrayHelper;
import adn.helpers.ArrayHelper.ArrayBuilder;
import adn.service.resource.engine.Finder;
import adn.service.resource.engine.FinderImpl;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.query.UpdateQuery;
import adn.service.resource.engine.template.ResourceTemplate;
import adn.service.resource.engine.tuple.ResourceTuplizer;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class PersistenceContext {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Finder finder;
	private final Map<String, Mutex> mutexMap = new MutexMap();

	@Autowired
	public PersistenceContext(@Autowired @Qualifier(FinderImpl.NAME) Finder finder) {
		super();
		this.finder = finder;
	}

	private Mutex createMutex(File file) {
		Mutex newMutex = new Mutex();

		mutexMap.put(file.getPath(), newMutex);

		return newMutex;
	}

	/**
	 * Retrieve a lock for the requested {@link File}. Create a new lock if there is
	 * currently no lock on it.
	 * 
	 * @param file file to lock
	 * @return the lock
	 */
	private Mutex obtainMutex(File file) {
		if (mutexMap.containsKey(file.getPath())) {
			return mutexMap.get(file.getPath());
		}

		return createMutex(file);
	}

	public boolean save(Query query, ResourceTemplate template, SQLException error) {
		String[] registeredColumnNames = template.getColumnNames();
		// extract values from the query, ignore those which are unknown
		Object[] values = Stream.of(registeredColumnNames).map(query::getParameterValue).toArray();
		ResourceTuplizer tuplizer = template.getTuplizer();

		tuplizer.validate(values, registeredColumnNames);

		File newFile = tuplizer.instantiate(values);

		if (finder.doesExist(newFile)) {
			error = new SQLException(String.format("Duplicate filename [%s]", newFile.getName()));
			return false;
		}
		// lock the new File
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

	public boolean update(UpdateQuery query, ResourceTemplate template, SQLException error) {
		ArrayBuilder<String> registeredColumns = ArrayHelper.from(template.getColumnNames());
		// extract update conditions, ignore unknowns
		String[] whereStatementColumnNames = Stream.of(query.getWhereStatementColumnNames())
				.filter(registeredColumns::contains).toArray(String[]::new);
		Object[] conditionValues = Stream.of(whereStatementColumnNames).map(query::getWhereConditionValue).toArray();
		ResourceTuplizer tuplizer = template.getTuplizer();
		// validate the update conditions
		tuplizer.validate(conditionValues, whereStatementColumnNames);
		// perform search
		File[] files = finder.find(template, conditionValues, whereStatementColumnNames);

		if (files.length == 0) {
			error = new SQLException("Unable to find any file for update");
			return false;
		}
		// extract updated values, ignore unknowns, reserve the array so that we don't
		// accidentally modify it while invoking optimised getter functions
		String[] setStatementColumnNames = Stream.of(query.getColumnNames()).filter(registeredColumns::contains)
				.toArray(String[]::new);
		final Object[] updatedValues = Stream.of(setStatementColumnNames).map(query::getParameterValue).toArray();
		// validate updated values
		tuplizer.validate(updatedValues, setStatementColumnNames);

		if (logger.isTraceEnabled()) {
			logger.trace(String.format("Found {%d} file(s) for update", files.length));
		}
		// optimised value getter functions:
		// use a function to produce value from the updatedValues array for columns on
		// which data is being updated. For those otherwise, use a function to produce a
		// value from the original File
		ArrayBuilder<String> setStatement = ArrayHelper.from(setStatementColumnNames);
		Stream<Function<File, Object>> extractingFunctions = Stream.of(template.getColumnNames()).map(columnName -> {
			if (setStatement.contains(columnName)) {
				return new Function<File, Object>() {
					@Override
					public Object apply(File t) {
						// each time we invoke ArrayBuilder#contains, found index will be recored so we
						// will use that index to locate the extracted value from the updatedValues
						// array
						return updatedValues[setStatement.getLastFoundIndex()];
					}
				};
			}

			return new Function<File, Object>() {
				@Override
				public Object apply(File t) {
					return tuplizer.getPropertyValue(t, template.getColumnIndex(columnName));
				}
			};
		});

		Object[] extractedValues;

		for (File file : files) {
			try {
				extractedValues = extractingFunctions.map(fnc -> fnc.apply(file)).toArray();

				synchronized (obtainMutex(file)) {
					if (logger.isTraceEnabled()) {
						logger.trace(String.format("Locking file [%s] for update", file.getPath()));
					}

					tuplizer.setPropertyValues(file, extractedValues);
				}
			} catch (RuntimeException rte) {
				rte.printStackTrace();
				error = new SQLException(rte);
				return false;
			}
		}

		return true;
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
