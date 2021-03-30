/**
 * 
 */
package adn.service.resource.models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import adn.service.resource.local.ResourceIdentifier;

/**
 * @author Ngoc Huy
 *
 */
@Entity
public class FileResource implements NamedResource {

	@Id
	@GeneratedValue
	@ResourceIdentifier
	private String pathname;

	private String name;

	private Date timestamp;

	private String directoryPath;

	private String extension;

	/**
	 * 
	 */
	public FileResource() {
		// TODO Auto-generated constructor stub
	}

	public FileResource(String directoryPath, String name, String extension, Date timestamp) {
		super();
		this.name = name;
		this.timestamp = timestamp;
		this.directoryPath = directoryPath;
		this.extension = "." + extension;
	}

	public String getPathname() {
		return pathname;
	}

	public void setPathname(String pathname) {
		this.pathname = pathname;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String getDirectoryPath() {
		return directoryPath;
	}

	public void setDirectoryPath(String directoryPath) {
		this.directoryPath = directoryPath;
	}

	@Override
	public String getExtension() {
		return extension;
	}

	void setExtension(String extension) {
		this.extension = extension;
	}

}
