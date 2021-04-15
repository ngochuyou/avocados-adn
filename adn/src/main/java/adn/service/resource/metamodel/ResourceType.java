/**
 * 
 */
package adn.service.resource.metamodel;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.hibernate.graph.spi.SubGraphImplementor;
import org.hibernate.metamodel.model.domain.internal.AbstractIdentifiableType;
import org.hibernate.metamodel.model.domain.spi.EntityTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.IdentifiableTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.PersistentAttributeDescriptor;
import org.hibernate.metamodel.model.domain.spi.SingularPersistentAttribute;

import adn.service.resource.local.ResourceManagerFactoryBuilder;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class ResourceType<D> extends AbstractIdentifiableType<D> implements EntityTypeDescriptor<D> {

	private final boolean hasSubclasses;
	private final Set<String> subclassNames;

	// @formatter:off
	public ResourceType(
		Class<D> entityType,
		String entityName,
		boolean hasIdentifier,
		boolean isVersioned,
		IdentifiableTypeDescriptor<? super D> superType, 
		boolean hasSubclasses) {
		super(
				entityType,
				entityName,
				superType,
				false,
				hasIdentifier,
				isVersioned,
				null
		);
		this.hasSubclasses = hasSubclasses;
		subclassNames = new HashSet<>();
		createInFlightAccess();
	}
	// @formatter:on

	interface InFlightAccess<D> extends IdentifiableTypeDescriptor.InFlightAccess<D> {

		void addSubclassName(String subclassName);

	}

	public class InFlightAccessImpl implements InFlightAccess<D> {

		final IdentifiableTypeDescriptor.InFlightAccess<D> superAccess;

		public InFlightAccessImpl(IdentifiableTypeDescriptor.InFlightAccess<D> superAccess) {
			// TODO Auto-generated constructor stub
			this.superAccess = superAccess;
		}

		@Override
		public void applyIdAttribute(SingularPersistentAttribute<D, ?> idAttribute) {
			// TODO Auto-generated method stub
			superAccess.applyIdAttribute(idAttribute);
		}

		@Override
		public void applyIdClassAttributes(Set<SingularPersistentAttribute<? super D, ?>> idClassAttributes) {
			// TODO Auto-generated method stub
			superAccess.applyIdClassAttributes(idClassAttributes);
		}

		@Override
		public void applyVersionAttribute(SingularPersistentAttribute<D, ?> versionAttribute) {
			// TODO Auto-generated method stub
			superAccess.applyVersionAttribute(versionAttribute);
		}

		@Override
		public void addAttribute(PersistentAttributeDescriptor<D, ?> attribute) {
			// TODO Auto-generated method stub
			superAccess.addAttribute(attribute);
		}

		@Override
		public void finishUp() {
			// TODO Auto-generated method stub
			superAccess.finishUp();
		}

		@Override
		public void addSubclassName(String subclassName) {
			// TODO Auto-generated method stub
			subclassNames.add(subclassName);
		}

	}

	@Override
	@SuppressWarnings("unchecked")
	protected InFlightAccess<D> createInFlightAccess() {
		// TODO Auto-generated method stub
		return new InFlightAccessImpl(super.createInFlightAccess());
	}

	@Override
	public InFlightAccess<D> getInFlightAccess() {
		// TODO Auto-generated method stub
		return (InFlightAccess<D>) super.getInFlightAccess();
	}

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

	public ResourceType<? super D> locateRootType() {
		if (getSupertype() == null) {
			return this;
		}

		return locateSuperType().locateRootType();
	}

	@Override
	public <S extends D> SubGraphImplementor<S> makeSubGraph(Class<S> subType) {
		// TODO Auto-generated method stub
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	public boolean hasSubclasses() {
		// TODO Auto-generated method stub
		return hasSubclasses;
	}

	public Set<String> getSubclassNames() {
		return subclassNames;
	}

	@Override
	public SubGraphImplementor<D> makeSubGraph() {
		// TODO Auto-generated method stub
		ResourceManagerFactoryBuilder.unsupport();
		return null;
	}

	public void consumeEach(Consumer<ResourceType<? super D>> consumer) {
		ResourceType<? super D> type = this;

		do {
			consumer.accept(type);
		} while ((type = type.locateRootType()) != null);
	}

}
