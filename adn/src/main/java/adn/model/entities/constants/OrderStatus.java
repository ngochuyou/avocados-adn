/**
 * 
 */
package adn.model.entities.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Ngoc Huy
 *
 */
public enum OrderStatus {

	PENDING_PAYMENT(100), PAID(200), EXPIRED(400), DELIVERING(300), FINISHED(999);

	private final int code;

	private OrderStatus(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	private static final Map<Integer, OrderStatus> STATUS_MAP;
	private static final Map<Integer, Integer> STATUS_KEY_MAP;

	static {
		Map<Integer, OrderStatus> statusMap = new HashMap<>();
		Map<Integer, Integer> keyMap = new HashMap<>();
		int pending = 100, paid = 200, expired = 400, delivering = 300, finished = 999;

		keyMap.put(pending, pending);
		keyMap.put(paid, paid);
		keyMap.put(expired, expired);
		keyMap.put(delivering, delivering);
		keyMap.put(finished, finished);

		statusMap.put(pending, PENDING_PAYMENT);
		statusMap.put(paid, PAID);
		statusMap.put(expired, EXPIRED);
		statusMap.put(delivering, DELIVERING);
		statusMap.put(finished, FINISHED);
		statusMap.put(null, null);

		STATUS_MAP = Collections.unmodifiableMap(statusMap);
		STATUS_KEY_MAP = Collections.unmodifiableMap(keyMap);
	}

	public static OrderStatus of(int code) {
		return Optional.ofNullable(STATUS_MAP.get(STATUS_KEY_MAP.get(code)))
				.orElseThrow(() -> new IllegalArgumentException(String.format(UNKNOWN_CODE_TEMPLATE, code)));
	}

	private static final String UNKNOWN_CODE_TEMPLATE = String.format("Unknown code %s for %s", "%d",
			OrderStatus.class.getSimpleName());

}
