/**
 * 
 */
package adn.service.resource.local;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.service.Service;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class Metadata implements Service {

	private final Map<String, Class<?>> imports = new HashMap<>();

	private volatile Set<String> processedImports = new HashSet<>();
	private volatile boolean allDone = processedImports.size() != imports.size();

	public void addImport(String name, Class<?> type) {
		imports.put(name, type);
	}

	public Map<String, Class<?>> getImports() {
		return imports;
	}

	public synchronized void markImportAsDone(String importName) {
		processedImports.add(importName);

		if (processedImports.size() == imports.size()) {
			allDone = true;
		}
	}

	public synchronized boolean isProcessingDone(String importName) {
		return processedImports.contains(importName);
	}

	public Set<String> getProcessedImports() {
		return processedImports;
	}

	public boolean isAllDone() {
		return allDone;
	}

}
