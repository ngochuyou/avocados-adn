/**
 * 
 */
package adn.model;

/**
 * @author Ngoc Huy
 *
 */
public abstract class Model extends AbstractModel {

	protected String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}