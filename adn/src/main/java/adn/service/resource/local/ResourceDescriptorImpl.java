/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.tuple.ValueGeneration;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceDescriptorImpl<T> implements ResourceDescriptor<T> {

	@Override
	public void build() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getResourceName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<T> getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Serializable getIdentifier(T instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIdentifier(T instance, Serializable identifier) {
		// TODO Auto-generated method stub

	}

	@Override
	public Getter getIdentifierGetter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Setter getIdentifierSetter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInstance(Class<? extends T> type) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isIdentifierAutoGenerated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ValueGeneration getIdentifierValueGeneration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceManagerFactory getResourceManagerFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isTransient(T instance) {
		// TODO Auto-generated method stub
		return false;
	}

}
