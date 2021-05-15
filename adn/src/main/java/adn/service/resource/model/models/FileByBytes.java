/**
 * 
 */
package adn.service.resource.model.models;

import java.io.File;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import adn.service.resource.LocalResource;
import adn.service.resource.type.FileContentByByteArrayType;

/**
 * @author Ngoc Huy
 *
 */
@LocalResource(systemType = File.class)
@Entity
@TypeDef(name = FileContentByByteArrayType.NAME, typeClass = FileContentByByteArrayType.class)
@Proxy(lazy = false)
public class FileByBytes extends FileResource {

	@Column(nullable = false)
	@Type(type = FileContentByByteArrayType.NAME)
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
