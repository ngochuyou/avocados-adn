/**
 * 
 */
package adn.service.resource.metamodel;

/**
 * @author Ngoc Huy
 *
 */
public enum EntityMode {

	POJO("pojo"),

	MAP("mapped-supperclass");

	private final String name;

	private EntityMode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
