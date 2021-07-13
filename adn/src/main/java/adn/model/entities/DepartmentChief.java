/**
 * 
 */
package adn.model.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Table(name = "department_chiefs")
public class DepartmentChief extends adn.model.entities.Entity {

	@EmbeddedId
	private DepartmentChiefId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("personnelId")
	@JsonIgnore
	private Personnel personnel;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("departmentId")
	@JsonIgnore
	private Department department;

	@Column(name = "end_date")
	private LocalDate endDate;

	public DepartmentChiefId getId() {
		return id;
	}

	public void setId(DepartmentChiefId id) {
		this.id = id;
	}

	public Personnel getPersonnel() {
		return personnel;
	}

	public void setPersonnel(Personnel personnel) {
		this.personnel = personnel;
	}

	public Department getDepartment() {
		return department;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

}

@SuppressWarnings("serial")
@Embeddable
class DepartmentChiefId implements Serializable {

	@Column(name = "personnel_id")
	private String personnelId;

	@Column(name = "department_id")
	private UUID departmentId;

	@CreationTimestamp
	@Column(name = "start_date", nullable = false, updatable = false)
	private LocalDate startDate;

	public DepartmentChiefId(String employeeId, UUID departmentId) {
		super();
		this.personnelId = employeeId;
		this.departmentId = departmentId;
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