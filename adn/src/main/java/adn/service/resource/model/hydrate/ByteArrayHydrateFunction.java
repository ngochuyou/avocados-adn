/**
 * 
 */
package adn.service.resource.model.hydrate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hibernate.HibernateException;

import adn.helpers.FunctionHelper.HandledFunction;

/**
 * @author Ngoc Huy
 *
 */
public class ByteArrayHydrateFunction implements HandledFunction<Object, byte[], HibernateException> {

	public static final int MAX_SIZE_IN_ONE_READ = 5 * 1024 * 1024;

	@Override
	public byte[] apply(Object arg) throws HibernateException {
		// TODO Auto-generated method stub
		try {
			Path path = ((File) arg).toPath();

			if (Files.size(path) > MAX_SIZE_IN_ONE_READ) {
				throw new HibernateException("File size is too large to read into byte[]");
			}

			return Files.readAllBytes(path);
		} catch (Exception e) {
			throw new HibernateException(e);
		}
	}

}
