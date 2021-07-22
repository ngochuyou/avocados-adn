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
import adn.service.resource.model.models.ProductImage;
import adn.service.resource.model.models.UserPhoto;

@Service
public class LocalResourceService implements ResourceService {

	@Autowired
	private ResourceManager session;

	private static final String USER_PHOTO_PATH;
	@SuppressWarnings("unused")
	private static final String PRODUCT_IMAGE_PATH;
	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	static {
		URI URI = new File(LocalStorage.DIRECTORY + UserPhoto.DIRECTORY).toURI();
		USER_PHOTO_PATH = URI.getPath();
		URI = new File(LocalStorage.DIRECTORY + ProductImage.DIRECTORY).toURI();
		PRODUCT_IMAGE_PATH = URI.getPath();
	}

	@Override
	public ServiceResult<String> uploadUserPhoto(MultipartFile file) {
		String originalFilename = file.getOriginalFilename();
		UserPhoto image = new UserPhoto();

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
	public <T extends ImageByBytes> byte[] getImageBytes(Class<T> type, String filename) {
		ImageByBytes resource = session.get(type, filename);

		return resource != null ? resource.getContent() : null;
	}

	@Override
	public ServiceResult<String> updateUserPhotoContent(MultipartFile file, String filename) {
		UserPhoto image = session.get(UserPhoto.class, filename);

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
		if (!doFlush) {
			return;
		}

		session.flush();
		session.close();
	}

	@Override
	public byte[] directlyGetImageBytes(String path, String filename) throws IOException {
		File file = new File(path + filename);

		if (!file.exists() || !file.isFile()) {
			return EMPTY_BYTE_ARRAY;
		}

		if (Files.size(file.toPath()) > LocalStorage.MAX_SIZE_IN_ONE_READ) {
			throw new IllegalArgumentException("File is too big to directly read");
		}

		return Files.readAllBytes(file.toPath());
	}

	@Override
	public byte[] directlyGetUserPhotoBytes(String filename) throws IOException {
		return directlyGetImageBytes(USER_PHOTO_PATH, filename);
	}

}
