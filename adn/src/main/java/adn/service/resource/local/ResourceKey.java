/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceKey<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Serializable identifier;

	private final ResourceDescriptor<T> descriptor;

	private final int hashCode;

	public ResourceKey(Serializable identifier, ResourceDescriptor<T> descriptor) {
		super();
		this.identifier = identifier;
		Assert.notNull(descriptor, "Resource descriptor can not be NULL");
		this.descriptor = descriptor;
		this.hashCode = getHashCode();
	}

	private int getHashCode() {
		int result = 17;

		String resourceName = descriptor.getResourceName();

		result = 37 * result + (resourceName != null ? resourceName.hashCode() : 0);
		result = 37 * result + descriptor.getIdentifierGetter().getReturnType().hashCode();

		return result;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return hashCode;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public boolean equals(Object other) {
		// TODO Auto-generated method stub
		if (!(other instanceof ResourceKey)) {
			return false;
		}

		ResourceKey entry = (ResourceKey<T>) other;

		return entry.identifier == identifier && (entry.descriptor == descriptor
				|| entry.descriptor.getResourceName().equals(descriptor.getResourceName()));
	}

	public Serializable getIdentifier() {
		return identifier;
	}

	public ResourceDescriptor<T> getDescriptor() {
		return descriptor;
	}

}
