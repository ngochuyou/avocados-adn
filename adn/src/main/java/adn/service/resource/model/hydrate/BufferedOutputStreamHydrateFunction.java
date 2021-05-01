/**
 * 
 */
package adn.service.resource.model.hydrate;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.hibernate.HibernateException;

import adn.helpers.FunctionHelper.HandledFunction;

/**
 * @author Ngoc Huy
 *
 */
public class BufferedOutputStreamHydrateFunction
		implements HandledFunction<Object, BufferedOutputStream, HibernateException> {

	@Override
	public BufferedOutputStream apply(Object arg) throws HibernateException {
		// TODO Auto-generated method stub
		try {
			return new BufferedOutputStream(new FileOutputStream((File) arg));
		} catch (Exception e) {
			throw new HibernateException(e);
		}
	}

}
