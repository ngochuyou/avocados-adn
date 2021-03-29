/**
 * 
 */
package adn.service.resource.local;

import org.springframework.lang.NonNull;

/**
 * @author Ngoc Huy
 *
 */
public interface LocalResourceStorage {

	boolean isFileExists(@NonNull String pathname);

}
