/**
 * 
 */
package adn.service.resource.models;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Entity
public class BufferedFile extends FileResource {

	@Column(nullable = true)
	private BufferedOutputStream os;

	@Column(nullable = true)
	private BufferedInputStream is;

	public BufferedFile() {
		super();
	}

	public BufferedFile(BufferedOutputStream os, BufferedInputStream is) {
		super();
		this.os = os;
		this.is = is;
	}

	public BufferedOutputStream getOs() {
		return os;
	}

	public void setOs(BufferedOutputStream os) {
		this.os = os;
	}

	public BufferedInputStream getIs() {
		return is;
	}

	public void setIs(BufferedInputStream is) {
		this.is = is;
	}

}
