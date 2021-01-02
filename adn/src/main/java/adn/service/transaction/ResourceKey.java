/**
 * 
 */
package adn.service.transaction;

import java.io.Serializable;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceKey implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5886267282822567669L;

	private final Serializable id;

	private final int hashCode;

	/**
	 * 
	 */
	public ResourceKey(Serializable id) {
		// TODO Auto-generated constructor stub
		this.id = id;
		this.hashCode = id.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		// TODO Auto-generated method stub
		if (!(other instanceof ResourceKey)) {
			return false;
		}

		ResourceKey that = (ResourceKey) other;

		return id == that.id;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return hashCode;
	}

}
