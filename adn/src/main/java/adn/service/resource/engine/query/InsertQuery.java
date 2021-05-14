/**
 * 
 */
package adn.service.resource.engine.query;

/**
 * @author Ngoc Huy
 *
 */
public class InsertQuery extends AbstractQuery {

	public InsertQuery(String[] columnNames, Object[] parameters) {
		super(QueryType.INSERT, columnNames, parameters);
	}

}
