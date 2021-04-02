/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.Setter;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceDescriptor<T> {

	String getResourceName();

	Class<T> getType();

	Serializable getIdentifier(T instance);

	void setIdentifier(T instance, Serializable identifier);

	Getter getIdentifierGetter();

	Setter getIdentifierSetter();

	boolean isInstance(Class<? extends T> type);

	boolean isIdentifierAutoGenerated();

	AnnotationBasedResourceValueGeneration getIdentifierValueGeneration();

	ResourceManagerFactory getResourceManagerFactory();

	void cleanUp();

	boolean isTransient(T instance);

}
