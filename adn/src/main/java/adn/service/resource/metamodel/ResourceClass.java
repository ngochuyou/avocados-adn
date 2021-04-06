/**
 * 
 */
package adn.service.resource.metamodel;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.mapping.AttributeContainer;
import org.hibernate.mapping.Property;

import adn.service.resource.local.ResourceManagerFactoryBuilder;

/**
 * @author Ngoc Huy
 *
 */
@Deprecated
public class ResourceClass<X> implements AttributeContainer {

	private ResourceClass<? super X> superClass;

	private String resourceName;
	private Class<X> type;

	private final List<ResourceProperty<X>> declaredProperties = new ArrayList<>();

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

	public ResourceClass<? super X> getSuperClass() {
		return superClass;
	}

	public void setSuperClass(ResourceClass<? super X> superClass) {
		this.superClass = superClass;
	}

	public List<ResourceProperty<X>> getDeclaredProperties() {
		return declaredProperties;
	}

	public void addProperty(ResourceProperty<X> other) {
		if (other.getResourceClass() != null && other.getResourceClass() != this) {
			throw new IllegalArgumentException("property already belongs to another ResourceClass");
		}

		declaredProperties.add(other);
		other.setResourceClass(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addProperty(Property attribute) {
		// TODO Auto-generated method stub
		if (!(attribute instanceof ResourceProperty)) {
			ResourceManagerFactoryBuilder.unsupport();
		}

		addProperty((ResourceProperty<X>) attribute);
	}

}
