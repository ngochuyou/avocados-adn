/**
 * 
 */
package adn.model.entities;

import java.time.LocalDateTime;

import adn.model.entities.metadata._ApprovableResource;

/**
 * @author Ngoc Huy
 *
 */
public interface ApprovableResource extends _ApprovableResource {

	ApprovalInformations getApprovalInformations();

	default LocalDateTime getApprovedTimestamp() {
		return getApprovalInformations().getApprovedTimestamp();
	}

	default void setApprovedTimestamp(LocalDateTime timestamp) {
		getApprovalInformations().setApprovedTimestamp(timestamp);
	}

	default Head getApprovedBy() {
		return getApprovalInformations().getApprovedBy();
	}

	default void setApprovedBy(Head head) {
		getApprovalInformations().setApprovedBy(head);
	}

}
