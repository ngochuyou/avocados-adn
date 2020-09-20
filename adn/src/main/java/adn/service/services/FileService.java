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

	protected final String nameNotEmpty = "FILENAME CAN NOT BE EMPTY";
	
	protected final String fileNotFound = "FILE NOT FOUND";
	
	public ServiceResult uploadFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return ServiceResult.bad();
		}

		String filename = new Date().getTime() + '-' + Strings.hash(file.getOriginalFilename()) + '.'
				+ FilenameUtils.getExtension(file.getOriginalFilename());

		try {
			byte[] bytes = file.getBytes();
			Path path = Paths.get(Constants.IMAGE_FILE_PATH + filename);

			Files.write(path, bytes);

			return ServiceResult.ok(filename);
		} catch (IOException e) {
			e.printStackTrace();

			return ServiceResult.status(ServiceStatus.FAILED);
		}
	}
	
	public ServiceResult removeFile(String filename) {
		if (Strings.isEmpty(filename)) {
			return ServiceResult.bad().setBody(nameNotEmpty);
		}
		
		File file = new File(Constants.IMAGE_FILE_PATH + filename);
		
		if (!file.exists()) {
			return ServiceResult.bad().setBody(fileNotFound);
		}
		
		file.delete();
		
		return ServiceResult.ok("The file " + filename + " successfully deleted");
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
			return null;
		}
	}

}
