/**
 * 
 */
package adn.test.application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import adn.service.resource.engine.query.Query;
import adn.service.resource.engine.query.QueryCompiler;

/**
 * @author Ngoc Huy
 *
 */
public class Solution {

	/**
	 * @param args
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SQLException
	 */
	public static void main(String[] args) throws NoSuchMethodException, SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException, SQLException {}

	@Test
	private void testCompileFind() throws SQLException {
		// @formatter:off
		String sql = ""
				+ "select imagebybyt0_.name as name1_0_0_, imagebybyt0_.createdDate as createdd2_0_0_, imagebybyt0_.extension as extensio3_0_0_, imagebybyt0_.lastModified as lastmodi4_0_0_, imagebybyt0_.content as content5_0_0_ "
				+ "from ImageByBytes imagebybyt0_ "
				+ "where imagebybyt0_.name=? ";
		// @formatter:on
		System.out.println(sql);
		Query query = QueryCompiler.compile(sql, null);
		System.out.println(query);
	}

	@Test
	public void test() throws IOException {
		File file = new File("C:\\Users\\Ngoc Huy\\Documents\\avocados-adn\\images\\aaaa.jpg");
		file = move(file, "bbbb.jpg");

		System.out.println(file.getPath());
		System.out.println(file.exists());
	}

	private File move(File file, String newName) throws IOException {
		Path source = Paths.get(file.getPath());

		Files.move(source, source.resolveSibling(newName));

		return new File(file.getParent() + "\\" + newName);
	}

}
