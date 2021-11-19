/**
 * 
 */
package adn.controller.query.impl;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import adn.application.context.ContextProvider;
import adn.application.context.builders.ModelContextProvider;
import adn.controller.query.RestQuery;
import adn.model.DomainEntity;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractRestQuery<T extends DomainEntity> implements RestQuery<T> {

	private final Class<T> entityType;

	private Set<String> columns;
	private final Set<String> associationColumns;
	private boolean hasAssociation = false;

	public AbstractRestQuery(Class<T> entityType, HashSet<String> associationColumns) {
		this.entityType = entityType;
		columns = new LinkedHashSet<>(
				ContextProvider.getBean(ModelContextProvider.class).getMetadata(entityType).getPropertiesSpan());
		this.associationColumns = associationColumns;
	}

	@Override
	public Set<String> getColumns() {
		return columns;
	}

	public final void setColumns(List<String> columns) {
		this.columns.addAll(columns);

		for (String column : this.columns) {
			if (associationColumns.contains(column)) {
				hasAssociation = true;
				return;
			}
		}
	}

	protected final <E extends DomainEntity> void setAssociated(RestQuery<E> association) {
		hasAssociation = (hasAssociation || association != null);
	}

	@Override
	public final boolean hasAssociation() {
		return hasAssociation;
	}

	@Override
	public final boolean hasColumns() {
		return !columns.isEmpty();
	}

	@Override
	public final boolean containsColumn(String columnName) {
		return columns.contains(columnName);
	}

	@Override
	public boolean hasCriteria() {
		return false;
	}

	@Override
	public Class<T> getEntityType() {
		return entityType;
	}

}
