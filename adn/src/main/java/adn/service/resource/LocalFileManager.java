/**
 * 
 */
package adn.service.resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import adn.application.context.ConfigurationContext;

/**
 * @author Ngoc Huy
 *
 */
public interface LocalFileManager {

	final String DIRECTORY_PATH = ConfigurationContext.getFileResourceDirectoryPath();

	final int MAX_SIZE_IN_ONE_READ = 3145728; // 3MB

	boolean supports(String filename);

	default FileMeta asserts(String pathname) {
		
		return new FileMeta(new File(DIRECTORY_PATH + pathname), this);
	}

	class FileMeta {

		protected final File file;

		private final LocalFileManager manager;

		private boolean check;

		public FileMeta(File file, LocalFileManager manager) {
			super();
			this.manager = manager;
			this.file = file;
			this.check = file.exists();
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

		public boolean check() {
			return check;
		}

	}

}
