/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Entity.class)
public abstract class AbstractEntityBuilder<T extends Entity> implements EntityBuilder<T> {
	
	@Override
	public <E extends T> E insertionBuild(Serializable id, E entity) {
		return entity;
	}
	
	@Override
	public <E extends T> E updateBuild(Serializable id, E entity, E persistence) {
		return entity;
	}
	
}
