/**
 * 
 */
package adn.service.resource.tuple;

/**
 * @author Ngoc Huy
 *
 */
public interface UnsavedValue {

	public Boolean isUnsaved(Object id);

	public Object getDefaultValue(Object currentValue);

}
