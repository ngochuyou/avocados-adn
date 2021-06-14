/**
 * 
 */
package adn.service.resource.model.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Persister;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import adn.application.Constants;
import adn.service.resource.ResourcePersisterImpl;
import adn.service.resource.annotation.Constructor;
import adn.service.resource.annotation.Content;
import adn.service.resource.annotation.Directory;
import adn.service.resource.annotation.LocalResource;
import adn.service.resource.model.type.FileContentByteArrayType;

/**
 * @author Ngoc Huy
 *
 */
@LocalResource
@Directory(path = Constants.IMAGE_STORAGE_DIRECTORY)
@Constructor(columnNames = FileResource.ID_NAME, argumentTypes = String.class)
@Persister(impl = ResourcePersisterImpl.class)
@Entity
@TypeDef(name = FileContentByteArrayType.NAME, typeClass = FileContentByteArrayType.class)
public class ImageByBytes extends FileResource {

	@Column(nullable = false)
	@Type(type = FileContentByteArrayType.NAME)
	@Content
	private byte[] content;

	public ImageByBytes() {
		super();
	}

	public ImageByBytes(String name, String extension, Date timestamp) {
		super(name, extension, timestamp);
	}

	public ImageByBytes(String name, String extension, Date timestamp, byte[] content) {
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
