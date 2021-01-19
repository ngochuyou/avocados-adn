/**
 * 
 */
package adn.service.resource.persistence;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;

import javax.persistence.LockModeType;

import org.hibernate.AssertionFailure;
import org.hibernate.engine.internal.AbstractEntityEntry;
import org.hibernate.engine.spi.Status;
import org.springframework.util.Assert;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class ResourceEntryImpl implements Serializable, ResourceEntry {

	private final Serializable id;

	private final transient ResourceKey key;

	private final Type type;

	private Object state;

	private Object version;

	private transient int compressedState;

	private final transient ResourcePersistenceContext persistenceContext;

	public ResourceEntryImpl(Serializable id, ResourceKey key, LockModeType lockMode, Status status, Type type,
			ResourcePersistenceContext context) {
		super();
		this.id = id;
		this.key = key;
		setCompressedValue(EnumState.STATUS, status);
		setCompressedValue(EnumState.LOCK_MODE, lockMode);
		this.type = type;
		this.persistenceContext = context;
	}

	@Override
	public Serializable getId() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public LockModeType getLockMode() {
		// TODO Auto-generated method stub
		return getCompressedValue(EnumState.LOCK_MODE);
	}

	@Override
	public void setLockMode(LockModeType lockMode) {
		// TODO Auto-generated method stub
		Assert.notNull(lockMode, "Entry lock mode can not be null");
		setCompressedValue(EnumState.LOCK_MODE, lockMode);
	}

	@Override
	public Status getStatus() {
		// TODO Auto-generated method stub
		return getCompressedValue(EnumState.STATUS);
	}

	@Override
	public void setStatus(Status status) {
		// TODO Auto-generated method stub
		Assert.notNull(status, "Entry status can not be null");
		setCompressedValue(EnumState.STATUS, status);
	}

	@Override
	public ResourceKey getKey() {
		// TODO Auto-generated method stub
		return key;
	}

	@Override
	public Object getState() {
		// TODO Auto-generated method stub
		return state;
	}

	@Override
	public Type getResourceType() {
		// TODO Auto-generated method stub
		return type;
	}

	@Override
	public Object getVersion() {
		// TODO Auto-generated method stub
		return version;
	}

	@Override
	public Object getPropertyValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void postUpdate(Object resourceInstance, Object updatedState, Object nextVersion) {
		// TODO Auto-generated method stub
	}

	@Override
	public void postDelete() {
		// TODO Auto-generated method stub
	}

	@Override
	public void postInsert(Object insertedState) {
		// TODO Auto-generated method stub
	}

	@Override
	public ResourcePersistenceContext getPersistenceContext() {
		// TODO Auto-generated method stub
		return persistenceContext;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "[resource_entry:" + id + '-' + getCompressedValue(EnumState.STATUS) + '-'
				+ getCompressedValue(EnumState.LOCK_MODE) + '-' + type.getTypeName() + ']';
	}

	@Override
	public boolean equals(Object other) {
		// TODO Auto-generated method stub
		if (!(other instanceof ResourceEntry)) {
			return false;
		}

		ResourceEntry entry = (ResourceEntry) other;

		return id.equals(entry.getKey()) && type.equals(entry.getResourceType());
	}

	@Override
	public void serialize(ObjectOutputStream oos) throws IOException {
		oos.writeObject(type);
		oos.writeObject(id);
		oos.writeObject(getStatus().name());
		oos.writeObject(state);
		oos.writeObject(version);
		oos.writeObject(getLockMode().toString());
	}

	/**
	 * @see AbstractEntityEntry
	 */
	protected <E extends Enum<E>> E getCompressedValue(EnumState<E> state) {
		// restore the numeric value from the bits at the right offset and return the
		// corresponding enum constant
		final int index = ((compressedState & state.getMask()) >> state.getOffset()) - 1;
		return index == -1 ? null : state.getEnumConstants()[index];
	}

	/**
	 * @see AbstractEntityEntry
	 */
	protected <E extends Enum<E>> void setCompressedValue(EnumState<E> state, E value) {
		// reset the bits for the given property to 0
		compressedState &= state.getUnsetMask();
		// store the numeric representation of the enum value at the right offset
		compressedState |= (state.getValue(value) << state.getOffset());
	}

	/**
	 * @see AbstractEntityEntry
	 */
	protected static class EnumState<E extends Enum<E>> {

		protected static final EnumState<LockModeType> LOCK_MODE = new EnumState<LockModeType>(0, LockModeType.class);
		protected static final EnumState<Status> STATUS = new EnumState<Status>(4, Status.class);

		protected final int offset;
		protected final E[] enumConstants;
		protected final int mask;
		protected final int unsetMask;

		private EnumState(int offset, Class<E> enumType) {
			final E[] enumConstants = enumType.getEnumConstants();

			// In case any of the enums cannot be stored in 4 bits anymore, we'd have to
			// re-structure the compressed
			// state int
			if (enumConstants.length > 15) {
				throw new AssertionFailure("Cannot store enum type " + enumType.getName() + " in compressed state as"
						+ " it has too many values.");
			}

			this.offset = offset;
			this.enumConstants = enumConstants;

			// a mask for reading the four bits, starting at the right offset
			this.mask = 0xF << offset;

			// a mask for setting the four bits at the right offset to 0
			this.unsetMask = 0xFFFF & ~mask;
		}

		/**
		 * Returns the numeric value to be stored for the given enum value.
		 */
		private int getValue(E value) {
			return value != null ? value.ordinal() + 1 : 0;
		}

		/**
		 * Returns the offset within the number value at which this enum value is
		 * stored.
		 */
		private int getOffset() {
			return offset;
		}

		/**
		 * Returns the bit mask for reading this enum value from the number value
		 * storing it.
		 */
		private int getMask() {
			return mask;
		}

		/**
		 * Returns the bit mask for resetting this enum value from the number value
		 * storing it.
		 */
		private int getUnsetMask() {
			return unsetMask;
		}

		/**
		 * Returns the constants of the represented enum which is cached for performance
		 * reasons.
		 */
		private E[] getEnumConstants() {
			return enumConstants;
		}
	}

}
