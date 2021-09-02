/**
 * 
 */
package adn.application;

import static adn.helpers.StringHelper.join;

/**
 * @author Ngoc Huy
 *
 */
public class Common {

	public static final String NOT_FOUND = "RESOURCE NOT FOUND";
	public static final String LOCKED = "RESOURCE WAS DEACTIVATED";
	public static final String INVALID_MODEL = "INVALID MODEL";
	public static final String EXISTED = "RESOURCE IS ALREADY EXSITED";
	public static final String FAILED = "Unable to complete task";
	public static final String ACCESS_DENIED = "ACCESS DENIDED";
	public static final String INVALID_SEARCH_CRITERIA = "Invalid search criteria";

	private static final String TEMPLATE = "%s %s";
	public static final String WHEN_APPROVED = "when resource is approved";
	public static final String WHEN_UNAPPROVED = "when resource hasn't been approved yet";
	
	public static String notFuture(String... prefix) {
		return String.format(TEMPLATE, join(prefix), " must not be in the future");
	}

	public static String notEmpty(String... prefix) {
		return String.format(TEMPLATE, join(prefix), " must not be empty");
	}

	public static String mustEmpty(String... prefix) {
		return String.format(TEMPLATE, join(prefix), " must not present");
	}

	public static String notNegative(String... prefix) {
		return String.format(TEMPLATE, join(prefix), " must not be negative");
	}

}
