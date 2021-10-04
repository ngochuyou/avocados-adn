/**
 * 
 */
package adn.application;

import static adn.helpers.StringHelper.join;
import static java.util.Map.entry;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import adn.helpers.StringHelper;

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

	public static final String MESSAGE = "message";
	public static final String ERROR = "error";

	public static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 10);

	public static String notFuture(String... prefix) {
		return String.format(TEMPLATE, join(prefix), " must not be in the future");
	}

	public static String notEmpty(String... prefix) {
		return String.format(TEMPLATE, join(prefix), " must not be empty");
	}

	public static String notNegative(String... prefix) {
		return String.format(TEMPLATE, join(prefix), " must not be negative");
	}

	public static String invalid(String... names) {
		return String.format("Invalid %s pattern", join(names));
	}

	public static String hasLength(String prefix, Integer min, Integer max) {
		String lead = StringHelper.get(prefix, "must").orElse("must");

		if (min != null && max != null) {
			return String.format("%s have the length between %d and %d", lead, min, max);
		}

		Function<Integer, String> plurality = (amount) -> amount > 1 ? "s" : "";

		if (min == null) {
			return String.format("%s have at most %d character%s", lead, max, plurality.apply(max));
		}

		return String.format("%s have at least %d character%s", lead, min, plurality.apply(min));
	}

	// @formatter:off
	private static final Map<Character, String> SYMBOL_NAMES = Map.ofEntries(
			entry('.', "period"),
			entry('(', "opening parenthesis"),
			entry(')', "closing parenthesis"),
			entry('\s', "space"),
			entry(',', "comma"),
			entry('-', "hyphen"),
			entry('_', "underscore"),
			entry('"', "quote"),
			entry('\'', "apostrophe"),
			entry('/', "slash"),
			entry('\\', "back slash"),
			entry('!', "exclamation"),
			entry('@', "at sign"),
			entry('#', "numero sign"),
			entry('$', "dollar sign"),
			entry('%', "percent sign"),
			entry('&', "ampersand"),
			entry('*', "asterisk")
			);
	// @formatter:on
	private static final String STRING_JOINER = ", ";

	public static final String symbolNamesOf(Character... characters) {
		return Stream.of(characters).map(c -> SYMBOL_NAMES.get(c)).filter(Objects::nonNull)
				.collect(Collectors.joining(STRING_JOINER));
	}

	public static final String MYSQL_CURRENCY_COLUMN_DEFINITION = "DECIMAL(13, 4)";
	public static final String MYSQL_UUID_COLUMN_DEFINITION = "BINARY(16)";
	public static final int MYSQL_TEXT_MAX_LENGTH = 65535;

	public static final String SHARED_TABLE_GENERATOR = "SHARED_TABLE_GENERATOR";
	public static final String SHARED_TABLE_GENERATOR_TABLENAME = "id_generators";

	public static final int CROCKFORD_10A = 1034;
	public static final int CROCKFORD_1A = 42;

}
