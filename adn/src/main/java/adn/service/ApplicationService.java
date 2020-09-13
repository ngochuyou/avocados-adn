package adn.service;

public interface ApplicationService {

	enum ServiceStatus {
		BAD(0, "INVALID INPUT"),

		OK(1, "SUCCESS"),
		
		FAILED(5, "SERVICE FAILED");
		
		private final int code;
		
		private final String message;

		ServiceStatus(int code, String message) {
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
