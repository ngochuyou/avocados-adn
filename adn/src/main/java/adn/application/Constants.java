/**
 * 
 */
package adn.application;

/**
 * @author Ngoc Huy
 *
 */
public class Constants {

	public static final String basePackage = "adn";
	
	public static final String entityPackage = "adn.model.entities";

	public static final String modelPackage = "adn.model.models";

	public static final String genericSpecificationPackage = "adn.model.specification.generic";

	public static final String genericFactoryPackage = "adn.model.factory.generic";

	public static final String genericDAOPackage = "adn.dao.generic";

	public static final String defaultEntityExtractorProdiverName = "defaultEntityExtractorProvider";
	
	public static final String defaultModelProducerProdiverName = "authenticationBasedProducerProvider";
	
	public static final String IMAGE_FILE_PATH = "C:\\Users\\Ngoc Huy\\Pictures\\avocados-adn\\";

	public static final String CONFIG_PATH = "C:\\Users\\Ngoc Huy\\Documents\\workspace\\avocados-adn\\config\\";

	public static final String DEFAULT_USER_PHOTO_NAME = "aad81c87bd8316705c4568e72577eb62476a.jpg";

	public static final String[] publicEndPoints = { "/account/photo\\GET" };
	// stolen from stackoverflow begins
	public static final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

	public static final String BCRYPT_REGEX = "^\\$2[ayb]\\$.{56}$";

	public static final String WHITESPACE_CHARS = "" /* dummy empty string for homogeneity */
			+ "\\u0009" // CHARACTER TABULATION
			+ "\\u000A" // LINE FEED (LF)
			+ "\\u000B" // LINE TABULATION
			+ "\\u000C" // FORM FEED (FF)
			+ "\\u000D" // CARRIAGE RETURN (CR)
			+ "\\u0020" // SPACE
			+ "\\u0085" // NEXT LINE (NEL)4
			+ "\\u00A0" // NO-BREAK SPACE
			+ "\\u1680" // OGHAM SPACE MARK
			+ "\\u180E" // MONGOLIAN VOWEL SEPARATOR
			+ "\\u2000" // EN QUAD
			+ "\\u2001" // EM QUAD
			+ "\\u2002" // EN SPACE
			+ "\\u2003" // EM SPACE
			+ "\\u2004" // THREE-PER-EM SPACE
			+ "\\u2005" // FOUR-PER-EM SPACE
			+ "\\u2006" // SIX-PER-EM SPACE
			+ "\\u2007" // FIGURE SPACE
			+ "\\u2008" // PUNCTUATION SPACE
			+ "\\u2009" // THIN SPACE
			+ "\\u200A" // HAIR SPACE
			+ "\\u2028" // LINE SEPARATOR
			+ "\\u2029" // PARAGRAPH SEPARATOR
			+ "\\u202F" // NARROW NO-BREAK SPACE
			+ "\\u205F" // MEDIUM MATHEMATICAL SPACE
			+ "\\u3000"; // IDEOGRAPHIC SPACE
}
