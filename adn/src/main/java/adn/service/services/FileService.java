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

	protected final String emptyName = "FILENAME CAN NOT BE EMPTY";

	protected final String fileNotFound = "FILE NOT FOUND";

	public String generateFilename(MultipartFile file) {

		return file != null
				? new Date().getTime() + '-' + Strings.hash(file.getOriginalFilename()) + '.'
						+ FilenameUtils.getExtension(file.getOriginalFilename())
				: null;
	}

	public ServiceResult uploadFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return ServiceResult.bad().setBody(emptyName);
		}

		String filename = new Date().getTime() + '-' + Strings.hash(file.getOriginalFilename()) + '.'
				+ FilenameUtils.getExtension(file.getOriginalFilename());

		try {
			byte[] bytes = file.getBytes();
			Path path = Paths.get(Constants.IMAGE_FILE_PATH + filename);

//			if (transactionFactory.getTransaction().getStrategy().equals(Transaction.Strategy.TRANSACTIONAL)) {
//				return ServiceResult.ok(toEvent(null,
//						Files.class.getDeclaredMethod("write", Path.class, byte[].class, OpenOption[].class),
//						new Object[] { path, bytes, new OpenOption[0] }, filename));
//			}

			Files.write(path, bytes);

			return ServiceResult.ok(filename);
		} catch (/* NoSuchMethodException | */ SecurityException | IOException e) {
			e.printStackTrace();

			return ServiceResult.status(ServiceStatus.FAILED)
					.setBody("Failed to put Files.write into transaction\n" + e.getMessage());
		}
	}

	public ServiceResult removeFile(String filename) {
		if (Strings.isEmpty(filename)) {
			return ServiceResult.bad().setBody(emptyName);
		}

		File file = new File(Constants.IMAGE_FILE_PATH + filename);

//		try {
//			File file = new File(Constants.IMAGE_FILE_PATH + filename);
//			
//			if (transactionFactory.getTransaction().getStrategy().equals(Transaction.Strategy.TRANSACTIONAL)) {
//				return ServiceResult.ok(toEvent(file, File.class.getDeclaredMethod("delete"), new Object[0],
//						filename + " was successfully deleted"));
//			}
//
//			if (!file.exists()) {
//				return ServiceResult.bad().setBody(fileNotFound);
//			}
//
//			file.delete();
//
//			return ServiceResult.ok(filename + " was successfully deleted");
//		} catch (NoSuchMethodException | SecurityException e) {
//			e.printStackTrace();
//
//			return ServiceResult.status(ServiceStatus.FAILED)
//					.setBody("Failed to put File.delete into transaction\n" + e.getMessage());
//		}

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
			return null;
		}
	}

}
