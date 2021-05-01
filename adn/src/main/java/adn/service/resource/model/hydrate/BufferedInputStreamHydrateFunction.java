/**
 * 
 */
package adn.service.resource.model.hydrate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.hibernate.HibernateException;

import adn.helpers.FunctionHelper.HandledFunction;

/**
 * @author Ngoc Huy
 *
 */
public class BufferedInputStreamHydrateFunction
		implements HandledFunction<Object, BufferedInputStream, HibernateException> {

	@Override
	public BufferedInputStream apply(Object arg) throws HibernateException {
		// TODO Auto-generated method stub
		try {
			return new BufferedInputStream(new FileInputStream((File) arg));
		} catch (Exception e) {
			throw new HibernateException(e);
		}
	}

}
