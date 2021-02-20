/**
 * 
 */
package adn.service.resource.metamodel;

import java.util.Set;

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import adn.application.context.ContextBuilder;

/**
 * @author Ngoc Huy
 *
 */
@Order(6)
@Component
public class AnnotationDrivenResourceMetamodel implements Metamodel, ContextBuilder {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void buildAfterStartUp() throws Exception {
		// TODO Auto-generated method stub
		logger.info(getLoggingPrefix(this) + "Building context: " + this.getClass());

		final String packageName = "adn.service.resource.model";

		logger.info(getLoggingPrefix(this) + "Finished building context: " + this.getClass());
	}

	@Override
	public <X> EntityType<X> entity(Class<X> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X> ManagedType<X> managedType(Class<X> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <X> EmbeddableType<X> embeddable(Class<X> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ManagedType<?>> getManagedTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<EntityType<?>> getEntities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<EmbeddableType<?>> getEmbeddables() {
		// TODO Auto-generated method stub
		return null;
	}

}
