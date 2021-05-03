/**
 * 
 */
package adn.service.resource.model.hydrate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.helpers.FunctionHelper.HandledFunction;

/**
 * @author Ngoc Huy
 *
 */
public class ByteArrayHydrateFunction implements HandledFunction<Object, byte[], HibernateException> {

	public static final int MAX_SIZE_IN_ONE_READ = 5 * 1024 * 1024;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public byte[] apply(Object arg) throws HibernateException {
		// TODO Auto-generated method stub
		try {
			Path path = ((File) arg).toPath();

			if (Files.size(path) > MAX_SIZE_IN_ONE_READ) {
				throw new HibernateException("File size is too large to read into byte[]");
			}

			byte[] bytes = Files.readAllBytes(path);

			logger.trace(String.format("extracted value ([%s] : [%s]) -> [%s]", "explicitly_hydrated", byte[].class,
					bytes.toString()));

			return bytes;
		} catch (Exception e) {
			throw new HibernateException(e);
		}
	}

}
