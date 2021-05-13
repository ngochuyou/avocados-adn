/**
 * 
 */
package adn.service.resource.model.models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Persister;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.annotations.UpdateTimestamp;

import adn.service.resource.LocalResource;
import adn.service.resource.ResourcePersisterImpl;
import adn.service.resource.factory.DefaultResourceIdentifierGenerator;
import adn.service.resource.type.FileCreationTimeStampType;
import adn.service.resource.type.FileExtensionType;

/**
 * @author Ngoc Huy
 *
 */
@LocalResource
@Entity
@Persister(impl = ResourcePersisterImpl.class)
// @formatter:off
@TypeDefs(value = {
	@TypeDef(name = FileCreationTimeStampType.NAME, typeClass = FileCreationTimeStampType.class),
	@TypeDef(name = FileExtensionType.NAME, typeClass = FileExtensionType.class)
})
//@formatter:on
@Proxy(lazy = false)
public class FileResource implements Resource {

	@Id
	@GeneratedValue(generator = DefaultResourceIdentifierGenerator.NAME)
	@GenericGenerator(strategy = DefaultResourceIdentifierGenerator.PATH, name = DefaultResourceIdentifierGenerator.NAME)
	private String name;

	@Type(type = FileCreationTimeStampType.NAME)
	private Date createdDate;

	@UpdateTimestamp
	private Date lastModified;

	@Type(type = FileExtensionType.NAME)
	private String extension;

	public FileResource() {}

	public FileResource(String pathname, String extension, Date timestamp) {
		super();
		this.name = pathname;
		this.createdDate = timestamp;
		this.extension = "." + extension;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
