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

	private final ResourcePersister<T> persister;

	private final int hashCode;

	public ResourceKey(Serializable identifier, ResourcePersister<T> persister) {
		super();
		this.identifier = identifier;
		Assert.notNull(persister, "Resource persister can not be NULL");
		this.persister = persister;
		this.hashCode = getHashCode();
	}

	private int getHashCode() {
		return 37 * 17 + persister.getIdentifierType().getReturnedClass().hashCode();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public boolean equals(Object other) {
		// TODO Auto-generated method stub
		if (!(other instanceof ResourceKey)) {
			return false;
		}

		ResourceKey entry = (ResourceKey<T>) other;

		return entry.identifier == identifier && entry.persister == persister;
	}

	public Serializable getIdentifier() {
		return identifier;
	}

	public ResourcePersister<T> getPersister() {
		return persister;
	}

	public int getHashcode() {
		// TODO Auto-generated method stub
		return hashCode;
	}

}
