/**
 * 
 */
package adn.model.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import adn.model.DomainComponentType;
import adn.model.entities.metadata._AuditableResource;
import adn.model.entities.metadata._User;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
@Embeddable
public class AuditInformations implements DomainComponentType, Serializable {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = _AuditableResource.$createdBy, referencedColumnName = _User.$id, updatable = false)
	private Operator createdBy;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = _AuditableResource.$lastModifiedBy, referencedColumnName = _User.$id)
	private Operator lastModifiedBy;

	@Column(name = _AuditableResource.$createdDate, nullable = false, updatable = false)
	private LocalDateTime createdDate;

	@Column(name = _AuditableResource.$lastModifiedDate, nullable = false)
	private LocalDateTime lastModifiedDate;

	public Operator getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Operator createdBy) {
		this.createdBy = createdBy;
	}

	public Operator getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(Operator lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}

	public LocalDateTime getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	@Override
	public int hashCode() {
		return Objects.hash(createdBy.getId(), createdDate, lastModifiedBy.getId(), lastModifiedDate);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		AuditInformations other = (AuditInformations) obj;

		return createdBy.getId().equals(other.createdBy.getId())
				&& lastModifiedBy.getId().equals(other.lastModifiedBy.getId()) && createdDate.equals(other.createdDate)
				&& lastModifiedDate.equals(other.lastModifiedDate);
	}

}
