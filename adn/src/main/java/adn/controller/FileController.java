/**
 * 
 */
package adn.controller;

import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import adn.service.resource.local.ResourceManager;
import adn.service.resource.models.FileResource;
import adn.service.services.FileService;

/**
 * @author Ngoc Huy
 *
 */
@Controller
@RequestMapping("/file")
public class FileController extends BaseController {

	@Autowired
	private FileService fileService;

	@Autowired
	private ResourceManager resourceManager;

	public static final String DEFAULT_USER_PHOTO_NAME = "aad81c87bd8316705c4568e72577eb62476a.jpg";

	public static final String IMAGE_FILE_PATH = "C:\\Users\\Ngoc Huy\\Pictures\\avocados-adn\\";

	protected FileResource toFileResource(String filename, Date timestamp) {
		String ext = FilenameUtils.getExtension(filename);

		return new FileResource(IMAGE_FILE_PATH, filename.substring(0, filename.indexOf(ext)), ext, timestamp);
	}

	@GetMapping("/public/image/bytes")
	public @ResponseBody ResponseEntity<?> getImageBytes(
			@RequestParam(name = "filename", required = true) String filename) {
		byte[] bytes = fileService.getImageBytes(filename);

		FileResource resource = toFileResource(filename, new Date());

		resourceManager.manage(resource, FileResource.class);

		resource = resourceManager.find(resource.getPathname(), FileResource.class);

		if (bytes == null || resource == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("FILE NOT FOUND");
		}

		return ResponseEntity.ok(null);
	}

}
