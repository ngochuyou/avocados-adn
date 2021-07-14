/**
 * 
 */
package adn.model.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import adn.model.entities.id.DepartmentChiefId;

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
	@JoinColumn(name = "personnel_id", updatable = false)
	@MapsId("personnelId")
	@JsonIgnore
	private Personnel personnel;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department_id", updatable = false)
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