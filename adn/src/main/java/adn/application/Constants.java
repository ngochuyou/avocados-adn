/**
 * 
 */
package adn.application;

/**
 * @author Ngoc Huy
 *
 */
public class Constants {

	public static final String LOCAL_DIRECTORY = "C:\\Users\\Ngoc Huy\\Documents\\avocados-adn\\";

	public static final String IMAGE_FILE_DIRECTORY = LOCAL_DIRECTORY + "images\\";

	public static final String DEFAULT_USER_PHOTO_NAME = "aad81c87bd8316705c4568e72577eb62476a.jpg";

	private Constants() {}

	public static final String ROOT_PACKAGE = "adn";

	public static final String ENTITY_PACKAGE = "adn.model.entities";

	public static final String MODEL_PACKAGE = "adn.model.models";

	public static final String RESOURCE_MODEL_PACKAGE = "adn.service.resource.models";

	public static final String GENERIC_SPECIFICATION_PACKAGE = "adn.model.specification.generic";

	public static final String GENERIC_FACTORY_PACKAGE = "adn.model.factory.generic";

	public static final String GENERIC_DAO_PACKAGE = "adn.dao.generic";

	public static final String DEFAULT_ENTITY_EXTRACTOR_PROVIDER_NAME = "defaultEntityExtractorProvider";

	public static final String DEFAULT_MODEL_PRODUCER_PROVIDER_NAME = "authenticationBasedProducerProvider";

	public static final String DEFAULT_RESOURCE_MANAGER_NAME = "defaultResourceManager";

}
