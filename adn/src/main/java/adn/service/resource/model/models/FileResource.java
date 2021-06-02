/**
 * 
 */
package adn.service.resource.model.models;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.AccessType;

import adn.service.resource.LocalResource;
import adn.service.resource.factory.DefaultResourceIdentifierGenerator;
import adn.service.resource.type.FileCreationTimeStampType;
import adn.service.resource.type.FileExtensionType;

/**
 * @author Ngoc Huy
 *
 */
// @formatter:off
@LocalResource(
	constructorParameterColumnNames = FileResource.ID_NAME,
	constructorParameterTypes = String.class
)
@MappedSuperclass
@TypeDefs(value = {
	@TypeDef(name = FileCreationTimeStampType.NAME, typeClass = FileCreationTimeStampType.class),
	@TypeDef(name = FileExtensionType.NAME, typeClass = FileExtensionType.class)
})
// @formatter:on
public class FileResource implements Resource {

	public static final String ID_NAME = "name";

	@Id
	@GeneratedValue(generator = DefaultResourceIdentifierGenerator.NAME)
	@GenericGenerator(strategy = DefaultResourceIdentifierGenerator.PATH, name = DefaultResourceIdentifierGenerator.NAME)
	@AccessType(value = AccessType.Type.PROPERTY)
	private String name;

	@Type(type = FileCreationTimeStampType.NAME)
	private Date createdDate;

	@Version
	@UpdateTimestamp
	@AccessType(value = AccessType.Type.PROPERTY)
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
