/**
 * 
 */
package adn.model.entities.metadata;

import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public class EntityMetadataImpl implements EntityMetadata {

	public EntityMetadataImpl() {
		
	}
	
	@Override
	public boolean hasAttribute(String attributeName) {
		return false;
	}

	@Override
	public <T> String validate(String attributeName, T value) {
		return null;
	}

	@Override
	public <T> String buildAttribute(String attributeName, T value) {
		return null;
	}

	@Override
	public <T> T produce(String attributeName, T value, Role role) {
		return null;
	}

}
