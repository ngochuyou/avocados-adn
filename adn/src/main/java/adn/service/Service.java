package adn.service;

import adn.application.context.ContextProvider;
import adn.helpers.ReflectHelper;

public interface Service {

	final ReflectHelper reflector = ContextProvider.getApplicationContext().getBean(ReflectHelper.class);

	public enum Status {

		BAD(0, "INVALID INPUT"),

		OK(1, "SUCCESS"),

		FAILED(5, "SERVICE FAILED");

		private final int code;

		private final String message;

		Status(int code, String message) {
			this.code = code;
			this.message = message;
		}

		public int code() {
			return this.code;
		}

		public String message() {
			return this.message;
		}

	}

}
