package adn.service.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import adn.service.internal.ResourceService;
import adn.service.internal.ServiceResult;
import adn.service.resource.ResourceManager;
import adn.service.resource.engine.LocalStorage;
import adn.service.resource.model.models.ImageByBytes;

@Service
public class LocalResourceService implements ResourceService {

	@Autowired
	private ResourceManager session;

	private static final URI imageURI;
	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	static {
		imageURI = new File(LocalStorage.DIRECTORY + ImageByBytes.DIRECTORY).toURI();
	}

	@Override
	public ServiceResult<String> uploadImage(MultipartFile file) {
		String originalFilename = file.getOriginalFilename();
		ImageByBytes image = new ImageByBytes();

		image.setName(originalFilename);
		image.setExtension("." + FilenameUtils.getExtension(originalFilename));

		try {
			image.setContent(file.getBytes());
			session.save(image);

			return ServiceResult.ok(image.getName() + image.getExtension());
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return ServiceResult.status(Status.FAILED);
		}
	}

	@Override
	public byte[] getImageBytes(String filename) {
		ImageByBytes image = session.get(ImageByBytes.class, filename);

		return image != null ? image.getContent() : null;
	}

	@Override
	public ServiceResult<String> updateContent(MultipartFile file, String filename) {
		ImageByBytes image = session.get(ImageByBytes.class, filename);

		if (image == null) {
			return ServiceResult.bad().body(String.format("Unable to find file [%s] for content-update", filename));
		}

		try {
			image.setContent(file.getBytes());

			session.update(image);

			return ServiceResult.ok(image.getName());
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return ServiceResult.status(Status.FAILED);
		}
	}

	@Override
	public void closeSession(boolean doFlush) {
		if (session == null) {
			return;
		}

		if (doFlush) {
			session.flush();
			return;
		}

		session.clear();
	}

	@Override
	public byte[] directlyGetImageBytes(String filename) throws IOException {
		File file = new File(imageURI.getRawPath() + filename);

		if (!file.exists() || !file.isFile()) {
			return EMPTY_BYTE_ARRAY;
		}

		if (Files.size(file.toPath()) > LocalStorage.MAX_SIZE_IN_ONE_READ) {
			throw new IllegalArgumentException("File is too big to directly read");
		}

		return Files.readAllBytes(file.toPath());
	}

}
