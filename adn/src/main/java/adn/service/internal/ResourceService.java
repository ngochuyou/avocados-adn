/**
 * 
 */
package adn.service.internal;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.springframework.web.multipart.MultipartFile;

import adn.service.resource.model.models.ImageByBytes;

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
	ServiceResult<String> uploadUserPhoto(MultipartFile file);

	/**
	 * Update the content of an image located by the current filename, using the
	 * provided {@link MultipartFile} for new content
	 * 
	 * @param file     the multipart-file
	 * @param filename filename of the updated image
	 * @return a {@link ServiceResult} contains a filename if success, null
	 *         otherwise
	 */
	ServiceResult<String> updateUserPhotoContent(MultipartFile file, String filename);

	ServiceResult<String> removeProductImages(Collection<String> filenames);
	
	ServiceResult<String[]> uploadProductImages(MultipartFile[] files);

	/**
	 * Read all of the bytes of the requested {@link File} using it's filename
	 * 
	 * @param filename
	 * @return
	 */
	<T extends ImageByBytes> byte[] getImageBytes(Class<T> type, String filename);

	byte[] directlyGetImageBytes(String path, String filename) throws IOException;

	byte[] directlyGetUserPhotoBytes(String filename) throws IOException;

	byte[] directlyGetProductImageBytes(String filename) throws IOException;
	
	void closeSession(boolean doFlush);

}
