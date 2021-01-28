package adn.service.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import adn.application.Constants;
import adn.service.ADNService;
import adn.service.ServiceResult;
import adn.utilities.Strings;

@Service
public class FileService implements ADNService {

	protected final String emptyName = "FILENAME CAN NOT BE EMPTY";

	protected final String fileNotFound = "FILE NOT FOUND";

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public String generateFilename(MultipartFile file) {
		return file != null
				? new Date().getTime() + '-' + Strings.hash(file.getOriginalFilename()) + '.'
						+ FilenameUtils.getExtension(file.getOriginalFilename())
				: null;
	}

	public ServiceResult<String> uploadFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return ServiceResult.bad().body(emptyName);
		}

		String filename = new Date().getTime() + '-' + Strings.hash(file.getOriginalFilename()) + '.'
				+ FilenameUtils.getExtension(file.getOriginalFilename());

		try {
			byte[] bytes = file.getBytes();
			Path path = Paths.get(Constants.IMAGE_FILE_PATH + filename);

			logger.debug("Writing file: " + filename);
			Files.write(path, bytes);

			return ServiceResult.ok(filename);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();

			return ServiceResult.status(Status.FAILED, String.class).body(e.getMessage());
		}
	}

	public ServiceResult<String> removeFile(String filename) {
		if (!Strings.hasLength(filename)) {
			return ServiceResult.bad().body(emptyName);
		}

		File file = new File(Constants.IMAGE_FILE_PATH + filename);

		if (!file.exists()) {
			return ServiceResult.bad().body(fileNotFound);
		}

		logger.debug("Writing file: " + filename);
		file.delete();

		return ServiceResult.ok(filename + " was successfully deleted");
	}

	public byte[] getImageBytes(String filename) {
		try {
			File file = new File(Constants.IMAGE_FILE_PATH + filename);

			if (!file.exists()) {
				return null;
			}

			byte[] data = Files.readAllBytes(file.toPath());

			return data;
		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}

}
