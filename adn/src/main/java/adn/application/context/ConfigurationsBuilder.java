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
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.ResourceUtils;

import adn.application.Constants;
import adn.security.SecurityResource;

/**
 * @author Ngoc Huy
 *
 */
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class ConfigurationsBuilder implements ContextBuilder {

	public static SecurityResource securityResource;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		logger.info("Intializing " + this.getClass().getName());
		this.readSecurityProperties();
		logger.info("Finished intializing " + this.getClass().getName());
	}

	private void readSecurityProperties() {
		ConfigurationsBuilder.securityResource = new SecurityResource();

		try {
			File file = ResourceUtils.getFile(Constants.CONFIG_PATH + "SpevIDMKW.txt");
			List<String> lines = Files.readAllLines(file.toPath());
			String nameValSeperator = lines.get(0);

			for (int i = 1; i < lines.size(); i++) {
				String[] pair = lines.get(i).split(nameValSeperator);
				String name = pair[0], val = pair[1];

				ConfigurationsBuilder.securityResource.getClass().getDeclaredField(name)
						.set(ConfigurationsBuilder.securityResource, val);
			}
		} catch (IOException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException
				| SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}