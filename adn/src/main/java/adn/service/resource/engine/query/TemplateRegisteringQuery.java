/**
 * 
 */
package adn.service.resource.engine.query;

import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.tuple.Instantiator;

/**
 * @author Ngoc Huy
 *
 */
public class TemplateRegisteringQuery extends AbstractQuery {

	private final PropertyAccess[] accessors;

	private final Instantiator instantiator;

	public TemplateRegisteringQuery(String templateName, String[] columnNames, Class<?>[] parameters,
			PropertyAccess[] accessors, Instantiator instantiator) {
		super(QueryType.REGISTER_TEMPLATE, columnNames, parameters);
		this.accessors = accessors;
		this.instantiator = instantiator;
	}

	public PropertyAccess[] getPropertyAccessors() {
		return accessors;
	}

	public Instantiator getInstantiator() {
		return instantiator;
	}

}
