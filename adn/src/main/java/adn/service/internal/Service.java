package adn.service.internal;

import adn.application.context.ContextProvider;
import adn.helpers.TypeHelper;

public interface Service {

	final TypeHelper reflector = ContextProvider.getApplicationContext().getBean(TypeHelper.class);
	
	final String UPLOAD_FAILURE = "Unable to upload file(s)";
	
	public enum Status {

		UNMODIFIED(3, "NOTHING CHANGED"),

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
