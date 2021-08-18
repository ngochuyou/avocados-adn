/**
 * 
 */
package adn.application.context.builders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.util.ResourceUtils;

import adn.application.context.ContextProvider;
import adn.application.context.internal.ContextBuilder;

/**
 * @author Ngoc Huy
 *
 */
public class ConfigurationContext implements ContextBuilder {

	private static transient $$$$$$$$$$$$$$$$$$$$$$$$$$$$ $$$;

	@Override
	public void buildAfterStartUp() {
		Logger logger = LoggerFactory.getLogger(this.getClass());
		logger.info("Building " + this.getClass().getName());
		this.____();
		logger.info("Finished building " + this.getClass().getName());
	}

	private void ____() {
		$$$ = new $$$$$$$$$$$$$$$$$$$$$$$$$$$$();

		try {
			File file = ResourceUtils.getFile(ContextBuilder.CONFIG_PATH + "SpevIDMKW.txt");
			List<String> $ = Files.readAllLines(file.toPath());

			if ($.size() < 1) {
				throw new IllegalStateException();
			}

			String _______ = $.get(0);

			for (int i = 1; i < $.size(); i++) {
				String[] __ = $.get(i).split(Pattern.quote(_______));

				if (__.length != 2) {
					continue;
				}

				String $$$$$$$ = __[0];
				String $_$_$_$_$ = __[1];

				try {
					$$$.getClass().getDeclaredField($$$$$$$).set($$$, $_$_$_$_$);
				} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
					continue;
				}
			}
		} catch (IOException | NoSuchFieldException e) {
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}
	}

	public static String getJwtAuthEndpoint() {
		return ConfigurationContext.$$$.jwtAuthEndpoint;
	}

	public static String getJwtSecretKey() {
		return ConfigurationContext.$$$.jwtSecretKey;
	}

	public static String getJwtAuthHeaderValue() {
		return ConfigurationContext.$$$.jwtAuthHeaderValue;
	}

	public static String getJwtCookieName() {
		return ConfigurationContext.$$$.jwtCookieName;
	}

	private class $$$$$$$$$$$$$$$$$$$$$$$$$$$$ {

		String jwtAuthEndpoint;

		String jwtSecretKey;

		String jwtAuthHeaderValue;

		String jwtCookieName;

	}

}
