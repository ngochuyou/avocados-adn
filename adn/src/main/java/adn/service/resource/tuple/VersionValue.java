/**
 * 
 */
package adn.service.resource.tuple;

/**
 * @author Ngoc Huy
 *
 */
public class VersionValue implements UnsavedValue {

	private final Object value;

	/**
	 * 
	 */
	public VersionValue() {
		// TODO Auto-generated constructor stub
		this.value = null;
	}

	public VersionValue(Object value) {
		// TODO Auto-generated constructor stub
		this.value = value;
	}

	public static final VersionValue NULL = new VersionValue() {

		@Override
		public final Boolean isUnsaved(Object version) {
			return version == null;
		}

		@Override
		public Object getDefaultValue(Object currentValue) {
			return null;
		}

		@Override
		public String toString() {
			return "VERSION_SAVE_NULL";
		}
	};

	public static final VersionValue UNDEFINED = new VersionValue() {
		@Override
		public final Boolean isUnsaved(Object version) {
			return version == null ? Boolean.TRUE : null;
		}

		@Override
		public Object getDefaultValue(Object currentValue) {
			return currentValue;
		}

		@Override
		public String toString() {
			return "VERSION_UNDEFINED";
		}
	};

	public static final VersionValue NEGATIVE = new VersionValue() {

		@Override
		public final Boolean isUnsaved(Object version) throws IllegalStateException {
			if (version == null) {
				return Boolean.TRUE;
			}

			if (version instanceof Number) {
				return ((Number) version).longValue() < 0L;
			}

			throw new IllegalStateException("unsaved-value NEGATIVE may only be used with short, int and long types");
		}

		@Override
		public Object getDefaultValue(Object currentValue) {
			return -1L;
		}

		@Override
		public String toString() {
			return "VERSION_NEGATIVE";
		}
	};

	@Override
	public Boolean isUnsaved(Object version) {
		// TODO Auto-generated method stub
		return version == null || version.equals(value);
	}

	@Override
	public Object getDefaultValue(Object currentValue) {
		// TODO Auto-generated method stub
		return value;
	}

}
