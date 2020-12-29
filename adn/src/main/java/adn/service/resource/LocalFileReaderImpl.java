/**
 * 
 */
package adn.service.resource;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class LocalFileReaderImpl implements LocalFileReader {

	protected final List<String> supportedTypes;

	/**
	 * 
	 */
	@Autowired
	public LocalFileReaderImpl() {
		// TODO Auto-generated constructor stub
		supportedTypes = Arrays.asList("jpg", "jpeg", "png", "txt");
	}

	@Override
	public boolean supports(String filename) {
		// TODO Auto-generated method stub
		return supportedTypes.contains(FilenameUtils.getExtension(filename).toLowerCase());
	}

	@Override
	public byte[] read(String filename) {
		// TODO Auto-generated method stub
		FileMeta meta;

		try {
			if (!(meta = asserts(filename).isSupported().inOneRead()).check()) {
				return null;
			}

			return Files.readAllBytes(Paths.get(meta.file.getPath()));
		} catch (IOException | IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public FileInputStream getFileInputStream(String filename) {
		// TODO Auto-generated method stub
		FileMeta meta;
		
		if (!(meta = asserts(filename).isSupported()).check()) {
			return null;
		}
		
		try {
			return new FileInputStream(meta.file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public BufferedReader getBufferedReader(String filename) {
		// TODO Auto-generated method stub
		FileMeta meta;
		
		if (!(meta = asserts(filename).isSupported()).check()) {
			return null;
		}
		
		try {
			return new BufferedReader(new FileReader(meta.file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
