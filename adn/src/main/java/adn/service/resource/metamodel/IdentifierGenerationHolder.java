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
public class IdentifierGenerationHolder implements ValueGeneration {

	public static final IdentifierGenerationHolder INSTANCE = new IdentifierGenerationHolder();
	
	private IdentifierGenerationHolder() {}
	
	@Override
	public boolean referenceColumnInSql() {
		unsupport();
		// TODO Auto-generated method stub
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
		return null;
	}

	@Override
	public String getDatabaseGeneratedReferencedColumnValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
