/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;

import org.hibernate.Session;

import adn.model.entities.PermanentEntity;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractPermanentEntityBuilder<T extends PermanentEntity> extends AbstractEntityBuilder<T> {

	@Override
	public <E extends T> E buildInsertion(Serializable id, E model, Session session) {
		model = super.buildInsertion(id, model, session);

		model.setActive(Boolean.TRUE);

		return model;
	}

}
