package adn.service.services;

import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import adn.service.ServiceResult;
import adn.service.resource.ResourceManager;
import adn.service.resource.model.models.ImageByBytes;

@Service
public class LocalResourceService implements ResourceService {

	@Autowired
	private ResourceManager session;

	@Override
	public ServiceResult<String> uploadImage(MultipartFile file) {
		String originalFilename = file.getOriginalFilename();
		ImageByBytes image = new ImageByBytes();

		image.setName(originalFilename);
		image.setExtension("." + FilenameUtils.getExtension(originalFilename));

		try {
			image.setContent(file.getBytes());

			session.save(image);

			return ServiceResult.ok(getFilename(image));
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

		try {
			image.setContent(file.getBytes());

			session.update(image);

			return ServiceResult.ok(getFilename(image));
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return ServiceResult.status(Status.FAILED);
		}
	}

	private String getFilename(ImageByBytes image) {
		return image.getName() + image.getExtension();
	}

}
