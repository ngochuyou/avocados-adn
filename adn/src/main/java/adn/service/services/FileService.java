package adn.service.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import adn.application.Constants;
import adn.service.ApplicationService;
import adn.service.ServiceResult;
import adn.utilities.Strings;

@Service
public class FileService implements ApplicationService {

	public ServiceResult uploadFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return new ServiceResult(ServiceStatus.BAD, null);
		}

		String filename = new Date().getTime() + '-' + Strings.hash(file.getOriginalFilename()) + '.'
				+ FilenameUtils.getExtension(file.getOriginalFilename());

		try {
			byte[] bytes = file.getBytes();
			Path path = Paths.get(Constants.IMAGE_FILE_PATH + filename);

			Files.write(path, bytes);
		} catch (IOException e) {
			e.printStackTrace();

			return new ServiceResult(ServiceStatus.FAILED, null);
		}

		return new ServiceResult(ServiceStatus.OK, filename);
	}

	public byte[] getImageBytes(String filename) throws IOException {
		File file = new File(Constants.IMAGE_FILE_PATH + filename);

		if (!file.exists()) {
			return null;
		}

		byte[] data = Files.readAllBytes(file.toPath());

		return data;
	}

}
