/**
 * 
 */
package adn.model.factory.property.production;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import adn.model.factory.ModelProducer;

/**
 * @author Ngoc Huy
 *
 */
public interface ModelPropertiesProducer extends ModelProducer<Object[], Map<String, Object>> {

	public static final Function<Object, Object> MASKER = val -> null;
	public static final Function<Object, Object> PUBLISHER = val -> val;

	@Override
	Map<String, Object> produce(Object[] source);

	@Override
	List<Map<String, Object>> produce(List<Object[]> source);

	Map<String, Object> singularProduce(Object source);

	List<Map<String, Object>> singularProduce(List<Object> source);

	Collection<String> validateAndTranslateColumnNames(Collection<String> requestedColumns) throws NoSuchFieldException;

}
