/**
 * 
 */
package adn.service.resource.model.models;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import javax.persistence.Column;

import adn.service.resource.local.LocalResource;
import adn.service.resource.metamodel.ExplicitlyHydrated;
import adn.service.resource.model.hydrate.BufferedInputStreamHydrateFunction;
import adn.service.resource.model.hydrate.BufferedOutputStreamHydrateFunction;

/**
 * @author Ngoc Huy
 *
 */
@LocalResource
public class BufferedFile extends FileResource {

	@Column(nullable = true)
	@ExplicitlyHydrated(byFunction = BufferedOutputStreamHydrateFunction.class)
	private BufferedOutputStream os;

	@Column(nullable = true)
	@ExplicitlyHydrated(byFunction = BufferedInputStreamHydrateFunction.class)
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
