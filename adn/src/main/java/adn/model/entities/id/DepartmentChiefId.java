/**
 * 
 */
package adn.model.entities.id;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.CreationTimestamp;

@SuppressWarnings("serial")
@Embeddable
public class DepartmentChiefId implements Serializable {

	@Column(name = "personnel_id")
	private String personnelId;

	@Column(name = "department_id", columnDefinition = "BINARY(16)")
	private UUID departmentId;

	@CreationTimestamp
	@Column(name = "start_date", nullable = false, updatable = false)
	private LocalDate startDate;

	public DepartmentChiefId() {}

	public DepartmentChiefId(String personnelId, UUID departmentId, LocalDate startDate) {
		super();
		this.personnelId = personnelId;
		this.departmentId = departmentId;
		this.startDate = startDate;
	}

	public String getPersonnelId() {
		return personnelId;
	}

	public void setPersonnelId(String personnelId) {
		this.personnelId = personnelId;
	}

	public UUID getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(UUID departmentId) {
		this.departmentId = departmentId;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	@Override
	public int hashCode() {
		return Objects.hash(departmentId, personnelId);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DepartmentChiefId)) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		DepartmentChiefId other = (DepartmentChiefId) obj;

		return Objects.equals(departmentId, other.departmentId) && personnelId.equals(other.personnelId)
				&& startDate.equals(other.startDate);
	}

}