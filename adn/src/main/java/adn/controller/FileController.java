/**
 * 
 */
package adn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

	@GetMapping("/public/image/bytes")
	public @ResponseBody ResponseEntity<?> getImageBytes(
			@RequestParam(name = "filename", required = true) String filename) {
		byte[] bytes = fileService.getImageBytes(filename);

		if (bytes == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("FILE NOT FOUND");
		}

		return ResponseEntity.ok(bytes);
	}

}
