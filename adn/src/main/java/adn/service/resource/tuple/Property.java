/**
 * 
 */
package adn.service.resource.tuple;

/**
 * @author Ngoc Huy
 *
 */
public class Property {

	private String name;

	private String cascade;

	private boolean nullable;
	private boolean updateable = true;
	private boolean insertable = true;
	private boolean optimisticLocked = true;

	private ValueGeneration valueGeneration;

	private String propertyAccessorName;

	public boolean isBackRef() {
		return false;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCascade() {
		return cascade;
	}

	public void setCascade(String cascade) {
		this.cascade = cascade;
	}

	public boolean getNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	public boolean getUpdateable() {
		return updateable;
	}

	public void setUpdateable(boolean updateable) {
		this.updateable = updateable;
	}

	public boolean getInsertable() {
		return insertable;
	}

	public void setInsertable(boolean insertable) {
		this.insertable = insertable;
	}

	public boolean getOptimisticLocked() {
		return optimisticLocked;
	}

	public void setOptimisticLocked(boolean optimisticLocked) {
		this.optimisticLocked = optimisticLocked;
	}

	public ValueGeneration getValueGenerationStrategy() {
		return valueGeneration;
	}

	public void setValueGenerationStrategy(ValueGeneration valueGenerationStrategy) {
		this.valueGeneration = valueGenerationStrategy;
	}

	public String getPropertyAccessorName() {
		return propertyAccessorName;
	}

	public void setPropertyAccessorName(String propertyAccessorName) {
		this.propertyAccessorName = propertyAccessorName;
	}

}
