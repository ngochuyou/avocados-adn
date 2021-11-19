/**
 * 
 */
package adn.model.entities;

import java.time.temporal.Temporal;

import adn.model.entities.metadata._SpannedResource;

/**
 * @author Ngoc Huy
 *
 */
public interface SpannedResource<T extends Temporal> extends _SpannedResource {

	T getAppliedTimestamp();
	
	T getDroppedTimestamp();

}
