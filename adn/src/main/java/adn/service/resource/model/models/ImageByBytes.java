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

import adn.application.Constants;
import adn.service.resource.LocalResource;
import adn.service.resource.engine.Content;
import adn.service.resource.type.FileContentByByteArrayType;

/**
 * @author Ngoc Huy
 *
 */
// @formatter:off
@LocalResource(
	constructorParameterColumnNames = FileResource.ID_NAME,
	constructorParameterTypes = String.class,
	directoryName = Constants.IMAGE_STORAGE_DIRECTORY
)
// @formatter:on
@Entity
@TypeDef(name = FileContentByByteArrayType.NAME, typeClass = FileContentByByteArrayType.class)
@Proxy(lazy = false)
public class ImageByBytes extends FileResource {

	@Column(nullable = false)
	@Type(type = FileContentByByteArrayType.NAME)
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
