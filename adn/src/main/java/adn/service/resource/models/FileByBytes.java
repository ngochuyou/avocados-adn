/**
 * 
 */
package adn.service.resource.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Entity
public class FileByBytes extends FileResource {

	@Column(nullable = false)
	private byte[] content;

	public FileByBytes(String directoryPath, String name, String extension, Date timestamp) {
		super(directoryPath, name, extension, timestamp);
	}

	public FileByBytes(String directoryPath, String name, String extension, Date timestamp, byte[] content) {
		super(directoryPath, name, extension, timestamp);
		this.content = content;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

}
