/**
 * 
 */
package adn.controller.query.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import adn.controller.query.filter.StringFilter;
import adn.model.entities.Factor;
import adn.model.entities.metadata._Factor;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractFactorQuery<T extends Factor> extends AbstractPermanentEntityQuery<T> {

	private static final Set<String> ASSOCIATION_COLUMNS = Set.of(_Factor.createdBy, _Factor.updatedBy,
			_Factor.approvedBy);

	public AbstractFactorQuery(Class<T> entityType, HashSet<String> associationColumns) {
		super(entityType, new HashSet<>(Stream.of(associationColumns, ASSOCIATION_COLUMNS).flatMap(set -> set.stream())
				.collect(Collectors.toSet())));
	}

	private StringFilter name;

	private AccountQuery createdBy;
	private AccountQuery updatedBy;
	private HeadQuery approvedBy;

	public StringFilter getName() {
		return name;
	}

	public void setName(StringFilter name) {
		this.name = name;
	}

	public AccountQuery getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(AccountQuery createdBy) {
		this.createdBy = createdBy;
		setAssociated(createdBy);
	}

	public AccountQuery getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(AccountQuery updatedBy) {
		this.updatedBy = updatedBy;
		setAssociated(updatedBy);
	}

	public HeadQuery getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(HeadQuery approvedBy) {
		this.approvedBy = approvedBy;
		setAssociated(approvedBy);
	}

	@Override
	public boolean hasCriteria() {
		return name != null || super.hasCriteria();
	}

}
