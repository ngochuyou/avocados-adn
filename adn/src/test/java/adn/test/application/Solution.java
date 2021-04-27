/**
 * 
 */
package adn.test.application;

import adn.service.resource.storage.LocalResourceStorage;

/**
 * @author Ngoc Huy
 *
 */
public class Solution {

	/**
	 * @param args
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static void main(String[] args) throws NoSuchMethodException, SecurityException {
		// TODO Auto-generated method stub
		String path = LocalResourceStorage.IMAGE_FILE_DIRECTORY + "abcjsz.jpg";

		System.out.println(path.substring(LocalResourceStorage.IMAGE_FILE_DIRECTORY.length()));
	}

}
