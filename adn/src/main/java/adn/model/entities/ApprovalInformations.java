/**
 * 
 */
package adn.model.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import adn.model.DomainComponentType;
import adn.model.entities.metadata._ApprovableResource;
import adn.model.entities.metadata._User;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
@Embeddable
public class ApprovalInformations implements DomainComponentType, Serializable {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = _ApprovableResource.$approvedBy, referencedColumnName = _User.$id, updatable = false)
	private Head approvedBy;

	@Column(name = _ApprovableResource.$approvedTimestamp, updatable = false)
	private LocalDateTime approvedTimestamp;

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
