/**
 * 
 */
package adn.service.resource.tuple;

import java.io.Serializable;

/**
 * @author Ngoc Huy
 *
 */
public class IdentifierValue implements UnsavedValue {

	private final Serializable value;

	public IdentifierValue(Serializable value) {
		super();
		this.value = value;
	}

	protected IdentifierValue() {
		this.value = null;
	}

	public static final IdentifierValue ANY = new IdentifierValue() {
		@Override
		public final Boolean isUnsaved(Object id) {
			return Boolean.TRUE;
		}

		@Override
		public Serializable getDefaultValue(Object currentValue) {
			return (Serializable) currentValue;
		}

		@Override
		public String toString() {
			return "SAVE_ANY";
		}
	};

	public static final IdentifierValue NONE = new IdentifierValue() {
		@Override
		public final Boolean isUnsaved(Object id) {
			return Boolean.FALSE;
		}

		@Override
		public Serializable getDefaultValue(Object currentValue) {
			return (Serializable) currentValue;
		}

		@Override
		public String toString() {
			return "SAVE_NONE";
		}
	};

	public static final IdentifierValue NULL = new IdentifierValue() {
		@Override
		public final Boolean isUnsaved(Object id) {
			return id == null;
		}

		@Override
		public Serializable getDefaultValue(Object currentValue) {
			return null;
		}

		@Override
		public String toString() {
			return "SAVE_NULL";
		}
	};

	public static final IdentifierValue UNDEFINED = new IdentifierValue() {
		@Override
		public final Boolean isUnsaved(Object id) {
			return null;
		}

		@Override
		public Serializable getDefaultValue(Object currentValue) {
			return null;
		}

		@Override
		public String toString() {
			return "UNDEFINED";
		}
	};

	@Override
	public Boolean isUnsaved(Object id) {
		// TODO Auto-generated method stub
		return id == null || id.equals(value);
	}

	@Override
	public Object getDefaultValue(Object currentValue) {
		// TODO Auto-generated method stub
		return value;
	}

}
