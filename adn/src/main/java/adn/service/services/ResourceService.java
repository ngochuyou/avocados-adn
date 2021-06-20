/**
 * 
 */
package adn.service.services;

import java.io.File;

import org.springframework.web.multipart.MultipartFile;

import adn.service.Service;
import adn.service.ServiceResult;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceService extends Service {

	/**
	 * Upload an image using the provided {@link MultipartFile}, destination could
	 * either be local or remote depends on implementations
	 * 
	 * @param file the multipart-file
	 * @return a {@link ServiceResult} contains a filename if success, null
	 *         otherwise
	 */
	ServiceResult<String> uploadImage(MultipartFile file);

	/**
	 * Update the content of an image located by the current filename, using the
	 * provided {@link MultipartFile} for new content
	 * 
	 * @param file the multipart-file
	 * @param filename filename of the updated image
	 * @return a {@link ServiceResult} contains a filename if success, null
	 *         otherwise
	 */
	ServiceResult<String> updateContent(MultipartFile file, String filename);

	/**
	 * Read all of the bytes of the requested {@link File} using it's filename
	 * 
	 * @param filename
	 * @return
	 */
	byte[] getImageBytes(String filename);

}
