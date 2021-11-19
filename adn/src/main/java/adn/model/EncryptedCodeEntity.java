/**
 * 
 */
package adn.model;

import java.io.Serializable;

/**
 * @author Ngoc Huy
 *
 */
public interface EncryptedCodeEntity {

	Serializable getId();

	void setCode(String encryption);

}
