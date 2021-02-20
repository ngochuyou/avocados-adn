/**
 * 
 */
package adn.application.context;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * @author Ngoc Huy
 *
 */
public interface ContextBuilder {

	void buildAfterStartUp() throws Exception;

	default String getLoggingPrefix(ContextBuilder builder) {
		Order anno = builder.getClass().getDeclaredAnnotation(Order.class);

		if (anno == null) {
			return "[UNKNOWN Order]";
		}

		switch (anno.value()) {
			case Ordered.HIGHEST_PRECEDENCE:
				return "[Ordered#HIGHEST]";
			case Ordered.LOWEST_PRECEDENCE:
				return "[Ordered#LOWEST]";
			default:
				return "[Ordered#" + anno.value() + "]";
		}
	}

}
