package adn.service.services;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import adn.application.Constants;
import adn.application.context.ServiceTransactionFactory;
import adn.service.ApplicationService;
import adn.service.ServiceResult;
import adn.service.builder.ServiceTransaction;
import adn.service.builder.TransactionalEvent;
import adn.service.builder.TransactionalService;
import adn.utilities.Strings;

@Service
public class FileService implements ApplicationService, TransactionalService {

	@Autowired
	private ServiceTransactionFactory transactionFactory;

	protected final String emptyName = "FILENAME CAN NOT BE EMPTY";

	protected final String fileNotFound = "FILE NOT FOUND";

	public String generateFilename(MultipartFile file) {

		return file != null
				? new Date().getTime() + '-' + Strings.hash(file.getOriginalFilename()) + '.'
						+ FilenameUtils.getExtension(file.getOriginalFilename())
				: null;
	}

	public ServiceResult uploadFile(MultipartFile file, String filename) {
		if (file == null || file.isEmpty()) {
			return ServiceResult.bad().setBody(emptyName);
		}

		filename = filename == null
				? new Date().getTime() + '-' + Strings.hash(file.getOriginalFilename()) + '.'
						+ FilenameUtils.getExtension(file.getOriginalFilename())
				: filename;

		try {
			byte[] bytes = file.getBytes();
			Path path = Paths.get(Constants.IMAGE_FILE_PATH + filename);

			if (transactionFactory.getTransaction().getStrategy()
					.equals(ServiceTransaction.TransactionStrategy.TRANSACTIONAL)) {
				boolean registrationResult = registerEvent(null,
						Files.class.getDeclaredMethod("write", Path.class, byte[].class, OpenOption[].class),
						new Object[] { path, bytes, new OpenOption[0] });

				if (!registrationResult) {
					throw new IOException("Failed to put Files.write into "
							+ transactionFactory.getTransaction().getClass().getName());
				}
			} else {
				Files.write(path, bytes);
			}

			return ServiceResult.ok(filename);
		} catch (NoSuchMethodException | SecurityException | IOException e) {
			e.printStackTrace();

			return ServiceResult.status(ServiceStatus.FAILED);
		}
	}

	public ServiceResult removeFile(String filename) {
		if (Strings.isEmpty(filename)) {
			return ServiceResult.bad().setBody(emptyName);
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

	@Override
	public boolean registerEvent(Object invoker, Method method, Object[] values) {
		// TODO Auto-generated method stub
		TransactionalEvent newEvent = new TransactionalEvent(invoker, method, values);

		this.transactionFactory.getTransaction().registerEvent(newEvent);

		return true;
	}

}
