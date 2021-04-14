/**
 * 
 */
package adn.service.resource.metamodel;

import org.hibernate.graph.spi.SubGraphImplementor;
import org.hibernate.metamodel.model.domain.internal.AbstractIdentifiableType;
import org.hibernate.metamodel.model.domain.spi.EntityTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.IdentifiableTypeDescriptor;

import adn.service.resource.local.ResourceManagerFactoryBuilder;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class ResourceType<D> extends AbstractIdentifiableType<D> implements EntityTypeDescriptor<D> {

	// @formatter:off
	public ResourceType(
		Class<D> entityType,
		String entityName,
		boolean hasIdentifier,
		boolean isVersioned,
		IdentifiableTypeDescriptor<? super D> superType) {
		super(
				entityType,
				entityName,
				superType,
				false,
				hasIdentifier,
				isVersioned,
				null
		);
	}
	// @formatter:on

	@Override
	public PersistenceType getPersistenceType() {
		// TODO Auto-generated method stub
		return PersistenceType.ENTITY;
	}

	@Override
	public BindableType getBindableType() {
		// TODO Auto-generated method stub
		return BindableType.ENTITY_TYPE;
	}

	@Override
	public Class<D> getBindableJavaType() {
		// TODO Auto-generated method stub
		return getJavaType();
	}

	public ResourceType<? super D> locateSuperType() {
		return (ResourceType<? super D>) getSupertype();
	}

	@Override
	public <S extends D> SubGraphImplementor<S> makeSubGraph(Class<S> subType) {
		// TODO Auto-generated method stub
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	@Override
	public SubGraphImplementor<D> makeSubGraph() {
		// TODO Auto-generated method stub
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}
}
