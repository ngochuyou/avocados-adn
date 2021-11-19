/**
 * 
 */
package adn.model.entities;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Auditable;

/**
 * @author Ngoc Huy
 *
 */
public interface AuditableResource<ID> extends Auditable<Operator, ID, LocalDateTime> {

	AuditInformations getAuditInformations();

	@Override
	default Optional<LocalDateTime> getCreatedDate() {
		return Optional.of(getAuditInformations().getCreatedDate());
	}

	@Override
	default void setCreatedDate(LocalDateTime timestamp) {
		getAuditInformations().setCreatedDate(timestamp);
	}

	@Override
	default Optional<LocalDateTime> getLastModifiedDate() {
		return Optional.of(getAuditInformations().getLastModifiedDate());
	}

	@Override
	default void setLastModifiedDate(LocalDateTime timestamp) {
		getAuditInformations().setLastModifiedDate(timestamp);
	}

	@Override
	default Optional<Operator> getCreatedBy() {
		return Optional.of(getAuditInformations().getCreatedBy());
	}

	@Override
	default void setCreatedBy(Operator operator) {
		getAuditInformations().setCreatedBy(operator);
	}

	@Override
	default Optional<Operator> getLastModifiedBy() {
		return Optional.of(getAuditInformations().getLastModifiedBy());
	}

	@Override
	default void setLastModifiedBy(Operator operator) {
		getAuditInformations().setLastModifiedBy(operator);
	}

	@Override
	default boolean isNew() {
		throw new UnsupportedOperationException();
	}

}
