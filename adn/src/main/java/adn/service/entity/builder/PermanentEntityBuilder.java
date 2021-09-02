/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import adn.model.Generic;
import adn.model.entities.PermanentEntity;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = PermanentEntity.class)
public class PermanentEntityBuilder<T extends PermanentEntity> extends AbstractEntityBuilder<T> {

	@Override
	public <E extends T> E buildInsertion(Serializable id, E model) {
		model = super.buildInsertion(id, model);

		model.setActive(Boolean.TRUE);

		return model;
	}

}
