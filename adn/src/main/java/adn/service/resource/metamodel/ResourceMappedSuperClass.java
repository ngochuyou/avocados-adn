/**
 * 
 */
package adn.service.resource.metamodel;

import static adn.service.resource.local.ResourceManagerFactory.unsupport;

import org.hibernate.mapping.MappedSuperclass;
import org.hibernate.mapping.PersistentClass;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceMappedSuperClass<X> extends MappedSuperclass {

	private final ResourceClass<? super X> superResourceClass;

	/**
	 * @param superMappedSuperclass
	 * @param superPersistentClass
	 */
	public ResourceMappedSuperClass(ResourceMappedSuperClass<? super X> superMappedSuperclass,
			ResourceClass<? super X> resourceClass) {
		super(superMappedSuperclass, null);
		// TODO Auto-generated constructor stub
		this.superResourceClass = resourceClass;
	}

	@Override
	public PersistentClass getSuperPersistentClass() {
		// TODO Auto-generated method stub
		unsupport();
		return super.getSuperPersistentClass();
	}

	public ResourceClass<? super X> getSuperResourceClass() {
		return superResourceClass;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ResourceMappedSuperClass<? super X> getSuperMappedSuperclass() {
		// TODO Auto-generated method stub
		return (ResourceMappedSuperClass<? super X>) super.getSuperMappedSuperclass();
	}

}
