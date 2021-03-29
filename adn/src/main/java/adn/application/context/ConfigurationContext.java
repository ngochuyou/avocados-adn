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

import adn.security.SecurityConfiguration;

/**
 * @author Ngoc Huy
 *
 */
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class ConfigurationContext implements ContextBuilder {

	private static SecurityResource securityConfiguration;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void buildAfterStartUp() {
		// TODO Auto-generated method stub
		logger.info(getLoggingPrefix(this) + "Building " + this.getClass().getName());
		this.readSecurityProperties();
		logger.info(getLoggingPrefix(this) + "Finished building " + this.getClass().getName());
	}

	private void readSecurityProperties() {
		securityConfiguration = new SecurityResource();

		try {
			File file = ResourceUtils.getFile(SecurityConfiguration.CONFIG_PATH + "SpevIDMKW.txt");
			List<String> lines = Files.readAllLines(file.toPath());

			if (lines.size() < 1) {
				throw new NoSuchFieldException("Could not build configuration. Invalid file format");
			}

			String nameValSeperator = lines.get(0);

			for (int i = 1; i < lines.size(); i++) {
				String[] pair = lines.get(i).split(nameValSeperator);

				if (pair.length != 2) {
					logger.trace("Skipping configuration line: " + lines.get(i)
							+ " since it does not match configuration format");
					continue;
				}

				String name = pair[0];
				String val = pair[1];

				try {
					securityConfiguration.getClass().getDeclaredField(name).set(securityConfiguration, val);
				} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
					logger.trace("Skipping property " + name + " since it was not included in configuration context");
					continue;
				}
			}
		} catch (IOException | NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
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

	private class SecurityResource {

		String jwtAuthEndpoint;

		String jwtSecretKey;

		String jwtAuthHeaderValue;

		String jwtCookieName;

	}

}
