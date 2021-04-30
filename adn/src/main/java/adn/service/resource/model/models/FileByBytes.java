/**
 * 
 */
package adn.service.resource.model.models;

import java.util.Date;

import javax.persistence.Column;

import adn.service.resource.local.LocalResource;
import adn.service.resource.metamodel.ExplicitlyHydrated;
import adn.service.resource.model.hydrate.HydrateByteArrayFunction;

/**
 * @author Ngoc Huy
 *
 */
@LocalResource
public class FileByBytes extends FileResource {

	@Column(nullable = false)
	@ExplicitlyHydrated(byFunction = HydrateByteArrayFunction.class)
	private byte[] content;

	public FileByBytes() {
		super();
	}

	public FileByBytes(String name, String extension, Date timestamp) {
		super(name, extension, timestamp);
	}

	public FileByBytes(String name, String extension, Date timestamp, byte[] content) {
		super(name, extension, timestamp);
		this.content = content;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}
}
