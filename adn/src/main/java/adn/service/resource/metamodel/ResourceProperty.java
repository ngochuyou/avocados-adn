/**
 * 
 */
package adn.service.resource.metamodel;

import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;

import adn.service.resource.local.ResourceManagerFactoryBuilder;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
@Deprecated
public class ResourceProperty<D> extends Property {

	private ResourceClass<D> owner;

	public ResourceProperty() {
		// TODO Auto-generated constructor stub
	}

	public ResourceProperty(ResourceClass<D> owner) {
		// TODO Auto-generated constructor stub
		this.owner = owner;
	}

	@Override
	public PersistentClass getPersistentClass() {
		// TODO Auto-generated method stub
		ResourceManagerFactoryBuilder.unsupport();
		return super.getPersistentClass();
	}

	public ResourceClass<D> getResourceClass() {
		// TODO Auto-generated method stub
		return owner;
	}

	public void setResourceClass(ResourceClass<D> owner) {
		this.owner = owner;
	}

}
