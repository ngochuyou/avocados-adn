/**
 * 
 */
package adn.service.resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.springframework.util.Assert;

import adn.application.context.ConfigurationContext;

/**
 * @author Ngoc Huy
 *
 */
public interface LocalFileManager {

	final String DIRECTORY_PATH = ConfigurationContext.getLocalFileResourceDirectoryPath();

	final int MAX_SIZE_IN_ONE_READ = 3145728;// 3MB

	boolean supports(String filename);

	default FileMeta getMeta(String pathname) {
		
		return new FileMeta(new File(DIRECTORY_PATH + pathname), this);
	}

	static final class FileMeta {

		private final LocalFileManager manager;
		
		final File file;

		final String extension;

		private boolean check;

		public FileMeta(File file, LocalFileManager manager) {
			super();
			Assert.notNull(manager, "LocalFileManager cannot be null");
			this.manager = manager;
			this.file = file;
			this.check = file.exists();
			this.extension = this.check ? FilenameUtils.getExtension(file.getPath()) : null;
		}

		FileMeta inOneRead() throws IOException {
			if (check) {
				check = Files.size(Paths.get(file.getPath())) < MAX_SIZE_IN_ONE_READ;

				return this;
			}

			return this;
		}

		FileMeta isSupported() {
			if (check) {
				check = manager.supports(file.getPath());	
			}
			
			return this;
		}

		public boolean asserts() {
			return check;
		}

	}

}
