/**
 * 
 */
package adn.service.resource.model.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import adn.service.resource.LocalResource;
import adn.service.resource.metamodel.type.ExplicitlyHydratedFileContextType;

/**
 * @author Ngoc Huy
 *
 */
@LocalResource
@Entity
@TypeDef(name = ExplicitlyHydratedFileContextType.NAME, typeClass = ExplicitlyHydratedFileContextType.class)
public class FileByBytes extends FileResource {

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
