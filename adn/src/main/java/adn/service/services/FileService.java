package adn.service.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
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
import adn.service.transaction.GlobalTransaction;
import adn.service.transaction.MethodEvent;
import adn.service.transaction.Transaction;
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

	public ServiceResult<String> uploadFile(MultipartFile file, GlobalTransaction transaction) {
		if (file == null || file.isEmpty()) {
			return ServiceResult.bad().body(emptyName);
		}

		String filename = new Date().getTime() + '-' + Strings.hash(file.getOriginalFilename()) + '.'
				+ FilenameUtils.getExtension(file.getOriginalFilename());

		try {
			byte[] bytes = file.getBytes();
			Path path = Paths.get(Constants.IMAGE_FILE_PATH + filename);
			// if a transaction is passed, register the final method and return the output
			if (transaction != null) {
				if (!transaction.getLockMode().equals(Transaction.LockMode.NONE)) {
					logger.debug("Cannot register event since transaction is locked." + transaction.getId()
							+ " LockMode: " + transaction.getLockMode());
					Files.write(path, bytes);

					return ServiceResult.ok(filename);
				}

				logger.debug("Registering action Files.write to transaction. Transaction id: " + transaction.getId());
				transaction.addAction(new MethodEvent<>(
						Files.class.getDeclaredMethod("write", Path.class, byte[].class, OpenOption[].class), null,
						Path.class, path, bytes, new OpenOption[0]));
			}

			return ServiceResult.ok(filename);
		} catch (SecurityException | IOException e) {
			e.printStackTrace();

			return ServiceResult.status(ServiceStatus.FAILED, String.class).body(e.getMessage());
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			return ServiceResult.status(ServiceStatus.FAILED, String.class)
					.body("Failed to register method into transaction\n" + e.getMessage());
		}
	}

	public ServiceResult<String> removeFile(String filename) {
		if (Strings.isEmpty(filename)) {
			return ServiceResult.bad().body(emptyName);
		}

		File file = new File(Constants.IMAGE_FILE_PATH + filename);

		if (!file.exists()) {
			return ServiceResult.bad().body(fileNotFound);
		}
		
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
