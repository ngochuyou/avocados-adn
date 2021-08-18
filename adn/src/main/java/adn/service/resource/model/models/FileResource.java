/**
 * 
 */
package adn.service.resource.model.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.AccessType;

import adn.service.resource.annotation.Extension;
import adn.service.resource.annotation.LocalResource;
import adn.service.resource.factory.DefaultResourceIdentifierGenerator;
import adn.service.resource.model.type.FileCreationTimeStampType;

/**
 * @author Ngoc Huy
 *
 */
// @formatter:off
@LocalResource
@MappedSuperclass
@TypeDefs(value = {
	@TypeDef(name = FileCreationTimeStampType.NAME, typeClass = FileCreationTimeStampType.class),
})
// @formatter:on
public abstract class FileResource implements Resource {

	public static final String ID_NAME = "name";

	@Id
	@GeneratedValue(generator = DefaultResourceIdentifierGenerator.NAME)
	@GenericGenerator(strategy = DefaultResourceIdentifierGenerator.PATH, name = DefaultResourceIdentifierGenerator.NAME)
	private String name;

	@CreationTimestamp
	@Type(type = FileCreationTimeStampType.NAME)
	@Column(updatable = false, nullable = false)
	private Date createdDate;

	@Version
	@UpdateTimestamp
	@AccessType(value = AccessType.Type.PROPERTY)
	@Column(nullable = false)
	private Date lastModified;

	@Extension
	@Column(nullable = false)
	private String extension;

	public FileResource() {}

	public FileResource(String pathname, String extension, Date timestamp) {
		super();
		this.name = pathname;
		this.createdDate = timestamp;
		this.extension = extension;
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

}
