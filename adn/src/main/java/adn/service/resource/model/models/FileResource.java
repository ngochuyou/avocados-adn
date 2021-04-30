/**
 * 
 */
package adn.service.resource.model.models;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import adn.service.resource.local.LocalResource;

/**
 * @author Ngoc Huy
 *
 */
@LocalResource
public class FileResource implements Resource {

	@Id
	@GeneratedValue
	@GenericGenerator(strategy = "resource_identifier", name = "resource_identifier")
	private String pathname;

	@CreationTimestamp
	private Date createdDate;

	@Version
	@UpdateTimestamp
	private Date lastModified;

	private String extension;

	public FileResource() {}

	public FileResource(String pathname, String extension, Date timestamp) {
		super();
		this.pathname = pathname;
		this.createdDate = timestamp;
		this.extension = "." + extension;
	}

	@Override
	public String getPathname() {
		return pathname;
	}

	public void setPathname(String pathname) {
		this.pathname = pathname;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

}
