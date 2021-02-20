/**
 * 
 */
package adn.service.resource.models;

import javax.persistence.Id;
import javax.persistence.Version;

import org.springframework.util.Assert;

/**
 * @author Ngoc Huy
 *
 */
public class FileResource {

	public static final transient String versionMark = ".v";

	@Id
	private String pathname;

	private byte[] content;

	@Version
	private String version;

	private String extension;

	public FileResource(String path, byte[] content, String version) {
		super();
		Assert.notNull(path, "Filepath can not be empty");
		this.pathname = path;
		this.content = content;
		this.version = version;
	}

	public String getPathname() {
		return pathname;
	}

	public void setPathname(String pathname) {
		this.pathname = pathname;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

}
