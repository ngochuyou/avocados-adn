/**
 * 
 */
package adn.service.resource;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Ngoc Huy
 *
 */
public interface LocalFileReader extends LocalFileManager {

	byte[] read(String filename) throws IOException, IllegalArgumentException;

	FileInputStream getFileInputStream(String filename) throws IOException, IllegalArgumentException;

	BufferedReader getBufferedReader(String filename) throws IOException, IllegalArgumentException;

}
