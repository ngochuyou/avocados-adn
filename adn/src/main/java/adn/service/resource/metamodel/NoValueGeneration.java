/**
 * 
 */
package adn.service.resource.metamodel;

import static adn.service.resource.local.ResourceManagerFactoryBuilder.unsupport;

import org.hibernate.tuple.GenerationTiming;
import org.hibernate.tuple.ValueGeneration;
import org.hibernate.tuple.ValueGenerator;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class NoValueGeneration implements ValueGeneration {

	public static final NoValueGeneration INSTANCE = new NoValueGeneration();
	
	private NoValueGeneration() {}
	
	@Override
	public boolean referenceColumnInSql() {
		// TODO Auto-generated method stub
		unsupport();
		return false;
	}

	@Override
	public ValueGenerator<?> getValueGenerator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GenerationTiming getGenerationTiming() {
		// TODO Auto-generated method stub
		return GenerationTiming.NEVER;
	}

	@Override
	public String getDatabaseGeneratedReferencedColumnValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
