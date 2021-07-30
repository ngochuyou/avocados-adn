package adn.service.services;

import static adn.service.internal.ServiceResult.bad;
import static adn.service.internal.ServiceResult.ok;
import static adn.service.internal.ServiceResult.status;
import static org.apache.commons.io.FilenameUtils.getExtension;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Collection;
import java.util.stream.Stream;

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
	private static final String PRODUCT_IMAGE_PATH;
	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	static {
		URI URI = new File(LocalStorage.DIRECTORY + UserPhoto.DIRECTORY).toURI();
		USER_PHOTO_PATH = URI.getPath();
		URI = new File(LocalStorage.DIRECTORY + ProductImage.DIRECTORY).toURI();
		PRODUCT_IMAGE_PATH = URI.getPath();
	}

	@SuppressWarnings("unchecked")
	private <T extends ImageByBytes> T[] createImageByBytes(Class<T> type, MultipartFile[] files)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, IOException {
		String originalFilename;
		T image;
		T[] results = (T[]) Array.newInstance(type, files.length);
		int i = 0;

		for (MultipartFile file : files) {
			if (file == null) {
				continue;
			}

			originalFilename = file.getOriginalFilename();
			image = type.getConstructor().newInstance();

			image.setName(originalFilename);
			image.setExtension("." + getExtension(originalFilename));
			image.setContent(file.getBytes());
			results[i++] = image;
		}

		return (T[]) results;
	}

	@Override
	public ServiceResult<String> uploadUserPhoto(MultipartFile file) {
		try {
			if (file == null) {
				return bad();
			}

			UserPhoto image = createImageByBytes(UserPhoto.class, new MultipartFile[] { file })[0];

			session.save(image);

			return ok(image.getName() + image.getExtension());
		} catch (Exception any) {
			any.printStackTrace();
			return status(Status.FAILED);
		}
	}

	@Override
	public ServiceResult<String> removeProductImages(Collection<String> filenames) {
		if (filenames.size() == 0) {
			return ServiceResult.<String>status(Status.UNMODIFIED).body(null);
		}

		try {
			ProductImage image;

			for (String filename : filenames) {
				image = session.get(ProductImage.class, filename);

				if (image == null) {
					return ServiceResult.<String>bad().body(String.format("File %s not found", filename));
				}

				session.delete(image);
			}

			return ok(null);
		} catch (Exception any) {
			return status(Status.FAILED);
		}
	}

	@Override
	public ServiceResult<String[]> uploadProductImages(MultipartFile[] files) {
		if (files.length == 0) {
			return ServiceResult.<String[]>status(Status.UNMODIFIED).body(new String[0]);
		}

		try {
			ProductImage[] images = createImageByBytes(ProductImage.class, files);

			for (ProductImage image : images) {
				session.save(image);
			}

			return ok(Stream.of(images).map(image -> image.getName() + image.getExtension()).toArray(String[]::new));
		} catch (Exception any) {
			any.printStackTrace();
			return status(Status.FAILED);
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
			return ServiceResult.<String>bad()
					.body(String.format("Unable to find file [%s] for content-update", filename));
		}

		try {
			image.setContent(file.getBytes());

			session.update(image);

			return ok(image.getName());
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return status(Status.FAILED);
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

	@Override
	public byte[] directlyGetProductImageBytes(String filename) throws IOException {
		return directlyGetImageBytes(PRODUCT_IMAGE_PATH, filename);
	}

}
