/**
 * 
 */
package adn.service.resource.engine.tuple;

import java.io.File;

import org.hibernate.tuple.Tuplizer;

import adn.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceTuplizer extends Tuplizer {

	/**
	 * Instantiate the resource
	 */
	@Override
	File instantiate();

	/**
	 * Instantiate the resource with the given arguments, this method assumes
	 * arguments types respect configured contract, type assertions should occur
	 * before calling
	 */
	File instantiate(Object[] arguments);

	/**
	 * Validate provided values to make sure that they respect whatever the
	 * configured business of creating a {@link File}
	 * 
	 * @param values
	 */
	void validate(Object[] values, String[] columnNames);

	interface ContentPropertyAccess extends PropertyAccessImplementor {

	}

}
