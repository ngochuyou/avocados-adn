/**
 * 
 */
package adn.service.resource.model.hydrate;

import static adn.helpers.FunctionHelper.reject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import org.hibernate.HibernateException;

import adn.helpers.FunctionHelper.HandledFunction;

/**
 * @author Ngoc Huy
 *
 */
public class HydrateByteArrayFunction implements HandledFunction<Object, byte[], HibernateException> {

	private final Map<Class<?>, Function<Object, byte[]>> hydrateFunctions = Map.of(File.class, this::fromFile);

	public static final int MAX_SIZE_IN_ONE_READ = 5 * 1024 * 1024;

	@Override
	public byte[] apply(Object arg) throws HibernateException {
		// TODO Auto-generated method stub
		return hydrateFunctions.containsKey(arg.getClass()) ? hydrateFunctions.get(arg.getClass()).apply(arg)
				: reject(new HibernateException(
						"Unable to apply ExplicitHydrate function since row type is not supported, given "
								+ arg.getClass()),
						HibernateException.class);
	}

	private byte[] fromFile(Object o) throws HibernateException {
		try {
			Path path = ((File) o).toPath();

			if (Files.size(path) > MAX_SIZE_IN_ONE_READ) {
				throw new HibernateException("File size is too large to read into byte[]");
			}

			return Files.readAllBytes(path);
		} catch (IOException e) {
			throw new HibernateException(e);
		}
	}

}
