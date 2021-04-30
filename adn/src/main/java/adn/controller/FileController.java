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

import adn.service.resource.model.models.FileByBytes;
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

	protected FileByBytes toFileResource(String filename, Date timestamp) {
		String ext = FilenameUtils.getExtension(filename);

		return new FileByBytes(filename.substring(0, filename.indexOf(ext)), ext, timestamp);
	}

	@GetMapping("/public/image/bytes")
	public @ResponseBody ResponseEntity<?> getImageBytes(
			@RequestParam(name = "filename", required = true) String filename) {
		byte[] bytes = fileService.getImageBytes(filename);

		FileByBytes resource = toFileResource(filename, new Date());

//		resourceManager.save(resource);

		if (bytes == null || resource == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("FILE NOT FOUND");
		}

		return ResponseEntity.ok(resource.getPathname());
	}

}
