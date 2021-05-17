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
import org.springframework.web.multipart.MultipartFile;

import adn.application.Constants;
import adn.helpers.StringHelper;
import adn.service.Service;
import adn.service.ServiceResult;

@org.springframework.stereotype.Service
public class FileService implements Service {

	protected final String emptyName = "FILENAME CAN NOT BE EMPTY";

	protected final String fileNotFound = "FILE NOT FOUND";

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public ServiceResult<String> uploadFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return ServiceResult.bad().body(emptyName);
		}

		String filename = new Date().getTime() + '-' + StringHelper.hash(file.getOriginalFilename()) + '.'
				+ FilenameUtils.getExtension(file.getOriginalFilename());

		try {
			byte[] bytes = file.getBytes();
			Path path = Paths.get(Constants.IMAGE_FILE_DIRECTORY + filename);

			logger.debug("Writing file: " + filename);
			Files.write(path, bytes);

			return ServiceResult.ok(filename);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();

			return ServiceResult.status(Status.FAILED, String.class).body(e.getMessage());
		}
	}

	public byte[] getImageBytes(String filename) {
		try {
			File file = new File(Constants.IMAGE_FILE_DIRECTORY + filename);

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
