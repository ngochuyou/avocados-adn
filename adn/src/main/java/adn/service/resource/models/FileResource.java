/**
 * 
 */
package adn.service.resource.models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author Ngoc Huy
 *
 */
@Entity
public class FileResource implements NamedResource {

	@Id
	@GeneratedValue
	private String pathname;

	private String name;

	private Date timestamp;

	private String directoryPath;

	/**
	 * 
	 */
	public FileResource() {
		// TODO Auto-generated constructor stub
	}

	public FileResource(String name, String firectoryPath, Date timestamp) {
		super();
		this.name = String.valueOf(name);
		this.timestamp = new Date(timestamp.getTime());
		this.directoryPath = firectoryPath;
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

}
