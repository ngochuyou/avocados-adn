/**
 * 
 */
package adn.factory;

/**
 * @author Ngoc Huy
 *
 */
public class EMProductionException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("rawtypes")
	private Class<? extends Factory> factoryClass;

	@SuppressWarnings("rawtypes")
	public EMProductionException(Class<? extends Factory> factoryClass) {
		super();
		this.factoryClass = factoryClass;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Production of " + factoryClass.getName() + " caused exception.";
	}

}
