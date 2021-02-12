/**
 * 
 */
package adn.service.resource;

import java.io.Serializable;

import javax.persistence.LockModeType;

import adn.service.resource.persister.ResourcePersister;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractResourceEntry implements ResourceEntry, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected final Serializable id;
	protected Object[] loadedState;
	protected Object version;
	protected final ResourcePersister persister;
	protected transient ResourceKey cachedEntityKey;
	protected final transient Object rowId;

	private transient int compressedState;

	// @formatter:off
	public AbstractResourceEntry(
			Status status,
			Object[] loadedState,
			Object rowId,
			Serializable id,
			Object version,
			LockModeType lockMode,
			boolean existsInStorage,
			ResourcePersister persister,
			boolean disableVersionIncrement) {
		// @formatter:on
		setCompressedValue(EnumState.STATUS, status);
		// not useful strictly speaking but more explicit
		setCompressedValue(EnumState.PREVIOUS_STATUS, null);
		// only retain loaded state if the status is not Status.READ_ONLY
		if (status != Status.READ_ONLY) {
			this.loadedState = loadedState;
		}
		this.id = id;
		this.rowId = rowId;
		setCompressedValue(BooleanState.EXISTS_IN_STORAGE, existsInStorage);
		this.version = version;
		setCompressedValue(EnumState.LOCK_MODE, lockMode);
		setCompressedValue(BooleanState.IS_BEING_REPLICATED, disableVersionIncrement);
		this.persister = persister;
	}

	@Override
	public LockModeType getLockMode() {
		// TODO Auto-generated method stub
		return getCompressedValue(EnumState.LOCK_MODE);
	}

	@Override
	public void setLockMode(LockModeType type) {
		// TODO Auto-generated method stub
		setCompressedValue(EnumState.LOCK_MODE, type);
	}

	@Override
	public Status getStatus() {
		// TODO Auto-generated method stub
		return getCompressedValue(EnumState.STATUS);
	}

	@Override
	public void setStatus(Status status) {
		// TODO Auto-generated method stub
		if (status == Status.READ_ONLY) {
			// memory optimization
			loadedState = null;
		}

		Status currentStatus = this.getStatus();

		if (currentStatus != status) {
			setCompressedValue(EnumState.PREVIOUS_STATUS, currentStatus);
			setCompressedValue(EnumState.STATUS, status);
		}
	}

	public Status getPreviousStatus() {
		return getCompressedValue(EnumState.PREVIOUS_STATUS);
	}

	@Override
	public Serializable getId() {
		// TODO Auto-generated method stub
		return id;
	}

	@Override
	public Object[] getLoadedState() {
		// TODO Auto-generated method stub
		return loadedState;
	}

	@Override
	public Object[] getDeletedState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDeletedState(Object[] deletedState) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isExistsStorage() {
		// TODO Auto-generated method stub
		return getCompressedValue(BooleanState.EXISTS_IN_STORAGE);
	}

	@Override
	public Object getVersion() {
		// TODO Auto-generated method stub
		return version;
	}

	@Override
	public ResourcePersister getPersister() {
		// TODO Auto-generated method stub
		return persister;
	}

	@Override
	public ResourceKey getResourceKey() {
		// TODO Auto-generated method stub
		return cachedEntityKey;
	}

	@Override
	public String getResourceName() {
		// TODO Auto-generated method stub
		return persister != null ? persister.getResourceName() : null;
	}

	@Override
	public boolean isBeingReplicated() {
		// TODO Auto-generated method stub
		return getCompressedValue(BooleanState.IS_BEING_REPLICATED);
	}

	@Override
	public Object getRowId() {
		// TODO Auto-generated method stub
		return rowId;
	}

	@Override
	public void postUpdate(Object resource, Object[] updatedState, Object nextVersion) {
		// TODO Auto-generated method stub
		loadedState = updatedState;
		setLockMode(LockModeType.WRITE);

		if (persister.isVersioned()) {
			version = nextVersion;
			persister.setPropertyValue(resource, persister.getVersionProperty(), nextVersion);
		}
	}

	@Override
	public void postDelete() {
		// TODO Auto-generated method stub
		setCompressedValue(EnumState.PREVIOUS_STATUS, getStatus());
		setCompressedValue(EnumState.STATUS, Status.GONE);
		setCompressedValue(BooleanState.EXISTS_IN_STORAGE, false);
	}

	@Override
	public void postInsert(Object[] insertedState) {
		// TODO Auto-generated method stub
		setCompressedValue(BooleanState.EXISTS_IN_STORAGE, true);
	}

	@Override
	public Object getLoadedValue(String propertyName) {
		// TODO Auto-generated method stub
		if (loadedState == null || propertyName == null) {
			return null;
		}

		return loadedState[persister.getResourceMetamodel().getPropertyIndexes().get(propertyName)];
	}

	@Override
	public void forceLocked(Object entity, Object nextVersion) {
		// TODO Auto-generated method stub
		version = nextVersion;
		loadedState[persister.getVersionProperty()] = nextVersion;
		setLockMode(LockModeType.PESSIMISTIC_FORCE_INCREMENT);
		persister.setPropertyValue(entity, getPersister().getVersionProperty(), nextVersion);
	}

	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		Status status = getStatus();

		if (status != Status.MANAGED && status != Status.READ_ONLY) {
			throw new IllegalStateException("Illegal state of resource");
		}

		return status == Status.READ_ONLY;
	}

	@Override
	public void setReadOnly(boolean readOnly, Object resource) {
		// TODO Auto-generated method stub
		if (readOnly == isReadOnly()) {
			return;
		}

		if (readOnly) {
			loadedState = null;
			return;
		}

		if (!persister.isMutable()) {
			throw new IllegalArgumentException("Cannot mutate an immutable resource");
		}

		setStatus(Status.MANAGED);
		loadedState = persister.getPropertyValues(resource);
	}

	protected <E extends Enum<E>> void setCompressedValue(EnumState<E> state, E value) {
		// reset the bits for the given property to 0
		compressedState &= state.getUnsetMask();
		// store the numeric representation of the enum value at the right offset
		compressedState |= (state.getValue(value) << state.getOffset());
	}

	protected <E extends Enum<E>> E getCompressedValue(EnumState<E> state) {
		// restore the numeric value from the bits at the right offset and return the
		// corresponding enum constant
		final int index = ((compressedState & state.getMask()) >> state.getOffset()) - 1;
		return index == -1 ? null : state.getEnumConstants()[index];
	}

	protected void setCompressedValue(BooleanState state, boolean value) {
		compressedState &= state.getUnsetMask();
		compressedState |= (state.getValue(value) << state.getOffset());
	}

	protected boolean getCompressedValue(BooleanState state) {
		return ((compressedState & state.getMask()) >> state.getOffset()) == 1;
	}

	protected static class EnumState<E extends Enum<E>> {

		protected static final EnumState<LockModeType> LOCK_MODE = new EnumState<LockModeType>(0, LockModeType.class);
		protected static final EnumState<Status> STATUS = new EnumState<Status>(4, Status.class);
		protected static final EnumState<Status> PREVIOUS_STATUS = new EnumState<Status>(8, Status.class);

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
				throw new IllegalStateException("Cannot store enum type " + enumType.getName()
						+ " in compressed state as" + " it has too many values.");
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

	protected enum BooleanState {

		EXISTS_IN_STORAGE(13), IS_BEING_REPLICATED(14);

		private final int offset;
		private final int mask;
		private final int unsetMask;

		private BooleanState(int offset) {
			this.offset = offset;
			this.mask = 0x1 << offset;
			this.unsetMask = 0xFFFF & ~mask;
		}

		private int getValue(boolean value) {
			return value ? 1 : 0;
		}

		/**
		 * Returns the offset within the number value at which this boolean flag is
		 * stored.
		 */
		private int getOffset() {
			return offset;
		}

		/**
		 * Returns the bit mask for reading this flag from the number value storing it.
		 */
		private int getMask() {
			return mask;
		}

		/**
		 * Returns the bit mask for resetting this flag from the number value storing
		 * it.
		 */
		private int getUnsetMask() {
			return unsetMask;
		}
	}

	@Override
	public boolean requiresDirtyCheck(Object entity) {
		// TODO Auto-generated method stub
		return isModifiableResource();
	}

	@Override
	public boolean isModifiableResource() {
		final Status status = getStatus();
		final Status previousStatus = getPreviousStatus();
		return getPersister().isMutable() && status != Status.READ_ONLY
				&& !(status == Status.DELETED && previousStatus == Status.READ_ONLY);
	}

}
