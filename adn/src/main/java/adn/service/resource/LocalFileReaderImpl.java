/**
 * 
 */
package adn.service.resource;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class LocalFileReaderImpl implements LocalFileReader {

	protected final List<String> supportedTypes = Arrays.asList("jpg", "jpeg", "png", "txt");

	protected final String UNSUPPORTED_EXTENSION_FORMAT = "Unsupported extension: %s on file: %s";

	@Override
	public boolean supports(String filename) {
		// TODO Auto-generated method stub
		return supportedTypes.contains(FilenameUtils.getExtension(filename).toLowerCase());
	}

	@Override
	public byte[] read(String filename) throws IOException, IllegalArgumentException {
		// TODO Auto-generated method stub
		FileMeta meta;

		if (!(meta = getMeta(filename).isSupported().inOneRead()).asserts()) {
			throw new IllegalArgumentException("Max chunk size exceeded in one read, filename: " + filename);
		}

		return Files.readAllBytes(Paths.get(meta.file.getPath()));
	}

	@Override
	public FileInputStream getFileInputStream(String filename) throws IOException, IllegalArgumentException {
		// TODO Auto-generated method stub
		FileMeta meta;

		if (!(meta = getMeta(filename).isSupported()).asserts()) {
			throw new IllegalArgumentException(String.format(UNSUPPORTED_EXTENSION_FORMAT, meta.extension, filename));
		}

		return new FileInputStream(meta.file);
	}

	@Override
	public BufferedReader getBufferedReader(String filename) throws IOException, IllegalArgumentException {
		// TODO Auto-generated method stub
		FileMeta meta;

		if (!(meta = getMeta(filename).isSupported()).asserts()) {
			throw new IllegalArgumentException(String.format(UNSUPPORTED_EXTENSION_FORMAT, meta.extension, filename));
		}

		return new BufferedReader(new FileReader(meta.file, Charset.forName("UTF-8")));
	}

}
