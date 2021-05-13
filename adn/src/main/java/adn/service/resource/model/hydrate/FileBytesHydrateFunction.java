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
public class FileBytesHydrateFunction implements HandledFunction<Object, byte[], HibernateException> {

	public static final FileBytesHydrateFunction INSTANCE = new FileBytesHydrateFunction();
	public static final int MAX_SIZE_IN_ONE_READ = 5 * 1024 * 1024; // 5MB

	private FileBytesHydrateFunction() {}

	@Override
	public byte[] apply(Object arg) throws HibernateException {
		// TODO Auto-generated method stub
		try {
			Path path = ((File) arg).toPath();

			if (Files.size(path) > MAX_SIZE_IN_ONE_READ) {
				throw new HibernateException(
						String.format("File size is too large to read into byte[], max size in one read is [%s] MB",
								MAX_SIZE_IN_ONE_READ));
			}

			return Files.readAllBytes(path);
		} catch (Exception e) {
			throw new HibernateException(e);
		}
	}

}
