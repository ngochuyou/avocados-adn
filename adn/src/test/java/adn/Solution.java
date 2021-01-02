/**
 * 
 */
package adn;

import org.hibernate.AssertionFailure;
import org.hibernate.LockMode;
import org.hibernate.engine.spi.Status;

/**
 * @author Ngoc Huy
 *
 */
public class Solution {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		int compressedState = 0;

		EnumState<LockMode> state = EnumState.LOCK_MODE;
		LockMode value = LockMode.WRITE;

		compressedState &= state.getUnsetMask();
		compressedState |= (state.getValue(value) << state.getOffset());

		EnumState<Status> state2 = EnumState.STATUS;
		Status value2 = Status.DELETED;

		compressedState &= state2.getUnsetMask();
		compressedState |= (state2.getValue(value2) << state2.getOffset());

		System.out.println(compressedState);

		int index = ((compressedState & state2.getMask()) >> state2.getOffset()) - 1;

		if (index == -1) {
			System.err.println(":NULL");

			return;
		}

		System.out.println(state2.getEnumConstants()[index]);

		return;
	}

	public static int powerByTwo(int a) {

		return 1 << a;
	}

}

class EnumState<E extends Enum<E>> {

	protected static final EnumState<LockMode> LOCK_MODE = new EnumState<LockMode>(0, LockMode.class);
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
	public int getValue(E value) {
		return value != null ? value.ordinal() + 1 : 0;
	}

	/**
	 * Returns the offset within the number value at which this enum value is
	 * stored.
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Returns the bit mask for reading this enum value from the number value
	 * storing it.
	 */
	public int getMask() {
		return mask;
	}

	/**
	 * Returns the bit mask for resetting this enum value from the number value
	 * storing it.
	 */
	public int getUnsetMask() {
		return unsetMask;
	}

	/**
	 * Returns the constants of the represented enum which is cached for performance
	 * reasons.
	 */
	public E[] getEnumConstants() {
		return enumConstants;
	}
}