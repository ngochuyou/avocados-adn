/**
 * 
 */
package adn.service.resource.tuple;

/**
 * @author Ngoc Huy
 *
 */
public interface ValueGeneration {

	GenerationTiming getGenerationTiming();
	
	ValueGenerator<?> getGenerator();
	
}
