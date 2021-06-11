/**
 * 
 */
package adn.service.resource.engine.action;

import java.io.File;
import java.util.Map;
import java.util.function.Function;

import adn.service.resource.engine.Storage;
import adn.service.resource.engine.query.Query;

/**
 * @author Ngoc Huy
 *
 */
public interface SaveAction {

	public static final Map<Class<?>, Function<Object, byte[]>> CONTENT_EXTRACTORS = Map.of(byte[].class,
			(bytes) -> (byte[]) bytes);

	void execute(Query query) throws RuntimeException;

	Storage getStorage();

	boolean performContentSave(File file, byte[] content) throws RuntimeException;

}
