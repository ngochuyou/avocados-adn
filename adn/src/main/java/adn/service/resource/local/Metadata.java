/**
 * 
 */
package adn.service.resource.local;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.service.Service;
import org.slf4j.LoggerFactory;

import adn.service.resource.local.factory.EntityManagerFactoryImplementor;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class Metadata implements Service, ManagerFactoryEventListener {

	private Map<String, Class<?>> imports = new HashMap<>();

	private volatile Set<String> processedImports = new HashSet<>();

	public Metadata() {
		listen();
	}

	public void addImport(String name, Class<?> type) {
		imports.put(name, type);
	}

	public Map<String, Class<?>> getImports() {
		return imports;
	}

	public synchronized void markImportAsDone(String importName) {
		processedImports.add(importName);
	}

	public synchronized boolean isProcessingDone(String importName) {
		return processedImports.contains(importName);
	}

	public Set<String> getProcessedImports() {
		return processedImports;
	}

	@Override
	public void postBuild(EntityManagerFactoryImplementor sessionFactory) {
		LoggerFactory.getLogger(this.getClass()).trace("Cleaning up " + this.getClass());
		processedImports = null;
	}

}
