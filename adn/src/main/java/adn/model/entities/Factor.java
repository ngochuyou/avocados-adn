/**
 * 
 */
package adn.model.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import adn.model.entities.metadata._Account;

/**
 * @author Ngoc Huy
 *
 */
@MappedSuperclass
public abstract class Factor extends PermanentEntity {

	@Column(nullable = false, unique = true)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by", referencedColumnName = _Account.id)
	private Operator createdBy;

	@CreationTimestamp
	@Column(name = "created_timestamp", nullable = false, updatable = false)
	private LocalDateTime createdTimestamp;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "updated_by", referencedColumnName = _Account.id)
	private Operator updatedBy;

	@UpdateTimestamp
	@Column(name = "updated_timestamp", nullable = false)
	private LocalDateTime updatedTimestamp;

	@Column(name = "deactivated_timestamp")
	private LocalDateTime deactivatedTimestamp;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "approved_by", referencedColumnName = _Account.id)
	private Head approvedBy;

	@Column(name = "approved_timestamp")
	private LocalDateTime approvedTimestamp;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Operator getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Operator createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	public Operator getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Operator updatedBy) {
		this.updatedBy = updatedBy;
	}

	public LocalDateTime getUpdatedTimestamp() {
		return updatedTimestamp;
	}

	public void setUpdatedTimestamp(LocalDateTime updatedTimestamp) {
		this.updatedTimestamp = updatedTimestamp;
	}

	public LocalDateTime getDeactivatedTimestamp() {
		return deactivatedTimestamp;
	}

	public void setDeactivatedTimestamp(LocalDateTime deactivatedTimestamp) {
		this.deactivatedTimestamp = deactivatedTimestamp;
	}

	public Head getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(Head approvedBy) {
		this.approvedBy = approvedBy;
	}

	public LocalDateTime getApprovedTimestamp() {
		return approvedTimestamp;
	}

	public void setApprovedTimestamp(LocalDateTime approvedTimestamp) {
		this.approvedTimestamp = approvedTimestamp;
	}

}
