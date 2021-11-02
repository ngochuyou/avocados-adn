/**
 * 
 */
package adn.application;

import static adn.helpers.StringHelper.join;
import static java.util.Map.entry;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

	/**
	 * 
	 */
	private static final String UNKNOWN_ERROR = "Unknown error";
	private static final String RESOURCE = "Resource";
	public static final String LOCKED = String.format("%s was locked", RESOURCE);
	public static final String INVALID_MODEL = "INVALID MODEL";
	public static final String FAILED = "Unable to complete task";
	public static final String ACCESS_DENIED = "Access denied";
	public static final String INVALID_SEARCH_CRITERIA = "Invalid search criteria";
	public static final String UPLOAD_FAILURE = "Unable to upload file(s)";

	public static final String COMMON_TEMPLATE = "%s %s";
	private static final String TIMESTAMP_ORDER_TEMPLATE = "%s must be before %s";
	private static final String NOT_FOUND_TEMPLATE = "%s not found";
	private static final String EXISTED_TEMPLATE = "%s has already existed";
	private static final String INVALID_PATTERN_TEMPLATE = "Invalid %s pattern";
	private static final String RANGED_LENGTH_TEMPLATE = "%s have the length between %d and %d";
	private static final String AT_MOST_LENGTH_TEMPLATE = "%s have at most %d character%s";
	private static final String AT_LEAST_LENGHT_TEMPLATE = "%s have at least %d character%s";

	private static final String PLURAL_FORM = "s";
	private static final String MUST = "must";
	public static final String WHEN_APPROVED = "when resource is approved";
	public static final String WHEN_UNAPPROVED = "when resource hasn't been approved";
	private static final String NOT_FUTURE = "must not be in the future";
	private static final String NOT_PAST = "must not be in the past";
	private static final String NOT_NEGATIVE = "must not be negative";
	private static final String NOT_EMPTY = "must not be empty";

	public static final String MESSAGE = "message";
	public static final String ERROR = "error";

	public static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 10);

	public static String notFuture(Object... prefix) {
		return String.format(COMMON_TEMPLATE, join(prefix), NOT_FUTURE);
	}
	
	public static String notPast(Object... prefix) {
		return String.format(COMMON_TEMPLATE, join(prefix), NOT_PAST);
	}

	public static String notEmpty(Object... prefix) {
		return String.format(COMMON_TEMPLATE, join(prefix), NOT_EMPTY);
	}

	public static String notNegative(Object... prefix) {
		return String.format(COMMON_TEMPLATE, join(prefix), NOT_NEGATIVE);
	}

	public static String sequential(String before, String after) {
		return String.format(TIMESTAMP_ORDER_TEMPLATE, before, after);
	}

	public static String invalid(Object... names) {
		return String.format(INVALID_PATTERN_TEMPLATE, join(names));
	}

	public static final Function<Integer, String> pluralizer = (amount) -> amount > 1 ? PLURAL_FORM
			: StringHelper.EMPTY_STRING;

	public static String hasLength(String prefix, Integer min, Integer max) {
		String lead = StringHelper.get(prefix, MUST).orElse(MUST);

		if (min != null && max != null) {
			return String.format(RANGED_LENGTH_TEMPLATE, lead, min, max);
		}

		if (min == null) {
			return String.format(AT_MOST_LENGTH_TEMPLATE, lead, max, pluralizer.apply(max));
		}

		return String.format(AT_LEAST_LENGHT_TEMPLATE, lead, min, pluralizer.apply(min));
	}

	public static String existed(Object... names) {
		if (names == null || names.length == 0) {
			return existed(RESOURCE);
		}

		return String.format(EXISTED_TEMPLATE, join(names));
	}

	public static String notfound(Object... names) {
		if (names == null || names.length == 0) {
			return notfound(RESOURCE);
		}

		return String.format(NOT_FOUND_TEMPLATE, join(names));
	}

	public static Map<String, String> message(String message) {
		return Map.of(MESSAGE, Optional.ofNullable(message).orElse(StringHelper.EMPTY_STRING));
	}

	public static Map<String, String> error(String error) {
		return Map.of(ERROR, Optional.ofNullable(error).orElse(UNKNOWN_ERROR));
	}

	public static final String COMMA = ",";
	public static final String DOT = ".";
	public static final String QUESTION_MARK = "?";

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
			entry('*', "asterisk"),
			entry('?', "question mark")
			);
	// @formatter:on
	private static final String STRING_JOINER = ", ";

	public static final String symbolNamesOf(Character... characters) {
		return Stream.of(characters).map(SYMBOL_NAMES::get).filter(Objects::nonNull)
				.collect(Collectors.joining(STRING_JOINER));
	}

	public static final String MYSQL_CURRENCY_COLUMN_DEFINITION = "DECIMAL(13, 4)";
	public static final String MYSQL_UUID_COLUMN_DEFINITION = "BINARY(16)";
	public static final String MYSQL_BIGINT_COLUMN_DEFINITION = "BIGINT";

	public static final String COMMON_LDT_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final DateTimeFormatter COMMON_LDT_FORMATTER = DateTimeFormatter.ofPattern(COMMON_LDT_FORMAT);
	public static final String COMMON_LD_FORMAT = "yyyy-MM-dd";
	public static final DateTimeFormatter COMMON_LD_FORMATTER = DateTimeFormatter.ofPattern(COMMON_LD_FORMAT);

	public static final int MYSQL_TEXT_MAX_LENGTH = 65535;

	public static final String SHARED_TABLE_GENERATOR = "SHARED_TABLE_GENERATOR";
	public static final String SHARED_TABLE_GENERATOR_TABLENAME = "id_generators";

	public static final int CROCKFORD_10A = 1034;
	public static final int CROCKFORD_1A = 42;

}
