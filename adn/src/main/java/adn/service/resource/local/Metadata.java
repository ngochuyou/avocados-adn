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
	
	public void addImport(String name, Class<?> type) {
		imports.put(name, type);
	}

	public Map<String, Class<?>> getImports() {
		return imports;
	}
	
	public void markImportAsDone(String importName) {
		processedImports.add(importName);
	}
	
	public synchronized boolean isProcessingDone(String importName) {
		return processedImports.contains(importName);
	}

}
