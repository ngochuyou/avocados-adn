/**
 * 
 */
package adn.service.resource.model.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.context.annotation.Lazy;

import adn.service.resource.LocalResource;
import adn.service.resource.type.ExplicitlyHydratedFileContextType;

/**
 * @author Ngoc Huy
 *
 */
@LocalResource
@Entity
@TypeDef(name = ExplicitlyHydratedFileContextType.NAME, typeClass = ExplicitlyHydratedFileContextType.class)
@Proxy(lazy = false)
public class FileByBytes extends FileResource {

	@Lazy(true)
	@Column(nullable = false)
	@Type(type = ExplicitlyHydratedFileContextType.NAME)
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
