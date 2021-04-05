/**
 * 
 */
package adn.service.resource.metamodel;

import static adn.service.resource.local.ResourceManagerFactory.unsupport;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.mapping.AttributeContainer;
import org.hibernate.mapping.Property;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceClass<X> implements AttributeContainer {

	private ResourceMappedSuperClass<? super X> superClass;

	private String resourceName;
	private Class<X> type;

	private final List<ResourceProperty<X>> properties = new ArrayList<>();

	private boolean hasIdentifier;
	private boolean isVersioned;
	private boolean isAbstract;

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public Class<X> getType() {
		return type;
	}

	public void setType(Class<X> type) {
		this.type = type;
	}

	public boolean hasIdentifier() {
		return hasIdentifier;
	}

	public void setHasIdentifier(boolean hasIdentifier) {
		this.hasIdentifier = hasIdentifier;
	}

	public boolean isVersioned() {
		return isVersioned;
	}

	public void setIsVersioned(boolean isVersioned) {
		this.isVersioned = isVersioned;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public void setIsAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	public ResourceMappedSuperClass<? super X> getSuperClass() {
		return superClass;
	}

	public void setSuperClass(ResourceMappedSuperClass<? super X> superClass) {
		this.superClass = superClass;
	}

	public List<ResourceProperty<X>> getProperties() {
		return properties;
	}

	public void addProperty(ResourceProperty<X> attribute) {
		properties.add(attribute);
		attribute.setResourceClass(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addProperty(Property attribute) {
		// TODO Auto-generated method stub
		if (!(attribute instanceof ResourceProperty)) {
			unsupport();
		}

		ResourceProperty<?> other = (ResourceProperty<?>) attribute;

		if (other.getResourceClass() != null && other.getResourceClass() != this) {
			throw new IllegalArgumentException("property already belongs to another ResourceClass");
		}

		addProperty((ResourceProperty<X>) attribute);
	}

}
