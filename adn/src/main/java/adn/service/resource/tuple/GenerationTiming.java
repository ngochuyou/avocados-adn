/**
 * 
 */
package adn.service.resource.tuple;

/**
 * @author Ngoc Huy
 *
 */
public enum GenerationTiming {

	NEVER {
		@Override
		public boolean includesInsert() {
			return false;
		}

		@Override
		public boolean includesUpdate() {
			return false;
		}
	},
	INSERT {
		@Override
		public boolean includesInsert() {
			return true;
		}

		@Override
		public boolean includesUpdate() {
			return false;
		}
	},
	ALWAYS {
		@Override
		public boolean includesInsert() {
			return true;
		}

		@Override
		public boolean includesUpdate() {
			return true;
		}
	};

	public abstract boolean includesInsert();

	public abstract boolean includesUpdate();

	public static GenerationTiming parseFromName(String name) {
		switch (name.toLowerCase()) {
		case "insert":
			return GenerationTiming.INSERT;
		case "always":
			return ALWAYS;
		default:
			return NEVER;
		}
	}

}
