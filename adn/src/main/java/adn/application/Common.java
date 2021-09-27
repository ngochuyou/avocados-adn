/**
 * 
 */
package adn.application;

import static adn.helpers.StringHelper.join;
import static java.util.Map.entry;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
	public static final String UPLOAD_FAILURE = "Unable to upload file(s)";

	private static final String TEMPLATE = "%s %s";
	public static final String WHEN_APPROVED = "when resource is approved";
	public static final String WHEN_UNAPPROVED = "when resource hasn't been approved yet";

	public static final String RESULT = "result";
	public static final String ERROR = "error";

	public static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 10);

	public static String notFuture(String... prefix) {
		return String.format(TEMPLATE, join(prefix), " must not be in the future");
	}

	public static String notEmpty(String... prefix) {
		return String.format(TEMPLATE, join(prefix), " must not be empty");
	}

	public static String mustEmpty(String... prefix) {
		return String.format(TEMPLATE, join(prefix), " must be empty");
	}

	public static String notNegative(String... prefix) {
		return String.format(TEMPLATE, join(prefix), " must not be negative");
	}

	// @formatter:off
	private static final Map<Character, String> SYMBOL_NAMES = Map.ofEntries(
			entry('.', "periods"),
			entry('(', "opening parentheses"),
			entry(')', "closing parentheses"),
			entry('\s', "spaces"),
			entry(',', "commas"),
			entry('_', "underscores"),
			entry('"', "quotes"),
			entry('\'', "apostrophe"),
			entry('/', "slashes"),
			entry('\\', "back slashes"),
			entry('!', "exclamations"),
			entry('@', "at signs"),
			entry('#', "numero signs"),
			entry('$', "dollar signs"),
			entry('%', "percent signs"),
			entry('&', "ampersands"),
			entry('*', "asterisks")
			);
	// @formatter:on
	private static final String STRING_JOINER = ", ";

	public static final String symbolNamesOf(Character... characters) {
		return Stream.of(characters).map(c -> SYMBOL_NAMES.get(c)).filter(Objects::nonNull)
				.collect(Collectors.joining(STRING_JOINER));
	}

	public static final String CURRENCY_MYSQL_COLUMN_DEFINITION = "DECIMAL(13, 4)";
	public static final String UUID_MYSQL_COLUMN_DEFINITION = "BINARY(16)";

}
