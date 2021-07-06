/**
 * 
 */
package adn.helpers;

import org.slf4j.Logger;

/**
 * @author Ngoc Huy
 *
 */
public class LoggerHelper {

	private LoggerHelper() {}

	public static LoggerBuilder with(Logger logger) {
		return new LoggerBuilder(logger);
	}

	public static class LoggerBuilder {

		Logger logger;

		public LoggerBuilder(Logger logger) {
			this.logger = logger;
		}

		public <T> T trace(String message, T val) {
			if (logger.isTraceEnabled()) {
				logger.trace(message);
			}

			return val;
		}

		public <T> T info(String message, T val) {
			if (logger.isInfoEnabled()) {
				logger.info(message);
			}

			return val;
		}

		public <T> T debug(String message, T val) {
			if (logger.isDebugEnabled()) {
				logger.debug(message);
			}

			return val;
		}

		public <T> T warn(String message, T val) {
			if (logger.isWarnEnabled()) {
				logger.warn(message);
			}

			return val;
		}

	}

}
