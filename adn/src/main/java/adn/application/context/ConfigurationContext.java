/**
 * 
 */
package adn.application.context;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.ResourceUtils;

import adn.application.Constants;

/**
 * @author Ngoc Huy
 *
 */
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class ConfigurationContext implements ContextBuilder {

	private static SecurityResource securityConfiguration;

	private static final String LOCAL_FILE_RESOURCE_DIRECTORY_PATH = "C:\\Users\\Ngoc Huy\\Documents\\avocados-adn\\";

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void buildAfterStartUp() {
		// TODO Auto-generated method stub
		logger.info("[HIGHEST]Intializing " + this.getClass().getName());
		this.readSecurityProperties();
		logger.info("[HIGHEST]Finished intializing " + this.getClass().getName());
	}

	private void readSecurityProperties() {
		securityConfiguration = new SecurityResource();

		try {
			File file = ResourceUtils.getFile(Constants.CONFIG_PATH + "SpevIDMKW.txt");
			List<String> lines = Files.readAllLines(file.toPath());

			if (lines.size() == 0) {
				throw new NoSuchFieldException("Could not build configuration. Invalid file format");
			}

			String nameValSeperator = lines.get(0);

			for (int i = 1; i < lines.size(); i++) {
				String[] pair = lines.get(i).split(nameValSeperator);
				String name = pair[0];
				String val = pair[1];

				securityConfiguration.getClass().getDeclaredField(name).set(securityConfiguration, val);
			}
		} catch (IOException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException
				| SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			SpringApplication.exit(context);
		}
	}

	public static String getJwtAuthEndpoint() {

		return ConfigurationContext.securityConfiguration.jwtAuthEndpoint;
	}

	public static String getJwtSecretKey() {

		return ConfigurationContext.securityConfiguration.jwtSecretKey;
	}

	public static String getJwtAuthHeaderValue() {

		return ConfigurationContext.securityConfiguration.jwtAuthHeaderValue;
	}

	public static String getJwtCookieName() {

		return ConfigurationContext.securityConfiguration.jwtCookieName;
	}

	public static String getLocalFileResourceDirectoryPath() {

		return ConfigurationContext.LOCAL_FILE_RESOURCE_DIRECTORY_PATH;
	}

	class SecurityResource {

		String jwtAuthEndpoint;

		String jwtSecretKey;

		String jwtAuthHeaderValue;

		String jwtCookieName;

	}

}
