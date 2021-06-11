/**
 * 
 */
package adn.service.resource.engine.tuple;

import java.io.File;

import org.hibernate.tuple.Tuplizer;

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
	 * Validate the passed values to make sure that they respect what ever the
	 * configured business of creating a {@link File}
	 * 
	 * @param values
	 */
	void validateValues(Object[] values);

}
