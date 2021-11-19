/**
 * 
 */
package adn.service.resource.engine;

import java.io.File;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.helpers.CollectionHelper;
import adn.helpers.CollectionHelper.ArrayBuilder;
import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.query.UpdateQuery;
import adn.service.resource.engine.template.ResourceTemplate;
import adn.service.resource.engine.tuple.ResourceTuplizer;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class PersistentResourceContext {

	private static final Logger logger = LoggerFactory.getLogger(PersistentResourceContext.class);

	private final Finder finder;
	private final Map<String, Mutex> mutexMap = new MutexMap();

	@Autowired
	public PersistentResourceContext(@Autowired Finder finder) {
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

	private void release(File file) {
		String path = file.getPath();

		if (!mutexMap.containsKey(path)) {
			// serious problem here, possible thread violation
			throw new IllegalThreadStateException((String.format("Unable to locate lock for [%s]", path)));
		}

		if (logger.isTraceEnabled()) {
			logger.trace(String.format("Releasing [%s] lock", file.getPath()));
		}

		mutexMap.remove(file.getPath());
	}

	public boolean delete(Query query, ResourceTemplate template, SQLException error) {
		Set<String> registeredColumns = new HashSet<>(Arrays.asList(template.getColumnNames()));
		// extract delete conditions, ignore unknowns
		String[] whereStatementColumnNames = Stream.of(query.getColumnNames()).filter(registeredColumns::contains)
				.toArray(String[]::new);
		Object[] conditionValues = Stream.of(whereStatementColumnNames).map(query::getParameterValue).toArray();
		ResourceTuplizer tuplizer = template.getTuplizer();
		// validate the delete conditions
		tuplizer.validate(conditionValues, whereStatementColumnNames);
		// perform search
		File[] files = finder.find(template, conditionValues, whereStatementColumnNames);

		if (files.length == 0) {
			error = new SQLException("Unable to find any file for delete");
			return false;
		}

		if (logger.isTraceEnabled()) {
			logger.trace(String.format("Found {%d} file(s) for delete", files.length));
		}

		File targetedFile = files[0];

		synchronized (obtainMutex(targetedFile)) {
			try {
				Files.delete(targetedFile.toPath());
			} catch (Exception any) {
				error = new SQLException(any);
				return false;
			} finally {
				release(targetedFile);
			}
		}

		return true;
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
			} finally {
				release(newFile);
			}
		}

		return true;
	}

	public boolean update(UpdateQuery query, ResourceTemplate template, SQLException error) {
		Set<String> registeredColumns = new HashSet<>(Arrays.asList(template.getColumnNames()));
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
			error = new SQLException("Unable to find any files for update");
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
		ArrayBuilder<String> setStatement = CollectionHelper.from(setStatementColumnNames);
		Stream<Function<File, Object>> extractingFunctions = Stream.of(template.getColumnNames()).map(columnName -> {
			if (setStatement.contains(columnName)) {
				// each time we invoke ArrayBuilder#contains, found index will be recored so we
				// will use that index to locate the extracted value from the updatedValues
				// array
				return file -> updatedValues[setStatement.getLastFoundIndex()];
			}

			return file -> tuplizer.getPropertyValue(file, template.getColumnIndex(columnName));
		});

		Object[] extractedValues;

		for (File file : files) {
			extractedValues = extractingFunctions.map(fnc -> fnc.apply(file)).toArray();

			synchronized (obtainMutex(file)) {
				try {
					if (logger.isTraceEnabled()) {
						logger.trace(String.format("Locking file [%s] for update", file.getPath()));
					}

					tuplizer.setPropertyValues(file, extractedValues);
				} catch (RuntimeException rte) {
					rte.printStackTrace();
					error = new SQLException(rte);
					return false;
				} finally {
					release(file);
				}
			}
		}

		return true;
	}

	private class Mutex {
	}

	class MutexMap extends LinkedHashMap<String, Mutex> {

		private static final long serialVersionUID = 1L;

	}

}
