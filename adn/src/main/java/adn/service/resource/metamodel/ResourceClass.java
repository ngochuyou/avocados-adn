/**
 * 
 */
package adn.service.resource.metamodel;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.mapping.Property;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceClass {

	private String resourceName;

	private Class<?> type;

	private List<Property> properties = new ArrayList<>();

	private List<Class<?>> propertyTypes = new ArrayList<>();

	public ResourceClass(String resourceName, Class<?> type) {
		super();
		this.resourceName = resourceName;
		this.type = type;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public List<Property> getProperties() {
		return properties;
	}

	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	public void addProperty(Property prop) {
		properties.add(prop);
	}

	public List<Class<?>> getPropertyTypes() {
		return propertyTypes;
	}

	public void setPropertyTypes(List<Class<?>> propertyTypes) {
		this.propertyTypes = propertyTypes;
	}

	public void addPropertyType(Class<?> type) {
		propertyTypes.add(type);
	}

}
