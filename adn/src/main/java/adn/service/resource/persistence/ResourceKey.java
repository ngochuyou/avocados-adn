/**
 * 
 */
package adn.service.resource.persistence;

import java.io.Serializable;
import java.lang.reflect.Type;

import org.springframework.util.Assert;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5886267282822567669L;

	private final Serializable identifier;

	private final Type type;
	
	private final int hashCode;

	/**
	 * 
	 */
	public ResourceKey(Serializable id, Type type) {
		// TODO Auto-generated constructor stub
		Assert.isTrue(type != null || id != null, "Resource identifier and type can not be null");
		this.identifier = id;
		this.type = type;
		this.hashCode = generateHashCode();
	}

	private int generateHashCode() {
		int result = 17;

		result = 37 * result + identifier.hashCode();
		result = 37 * result + type.hashCode();
		
		return result;
	}

	@Override
	public boolean equals(Object other) {
		// TODO Auto-generated method stub
		if (!(other instanceof ResourceKey)) {
			return false;
		}

		ResourceKey otherKey = (ResourceKey) other;
		
		return identifier.equals(otherKey.identifier) && type.equals(otherKey.type);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return hashCode;
	}

	public Serializable getIdentifier() {
		return identifier;
	}

}
