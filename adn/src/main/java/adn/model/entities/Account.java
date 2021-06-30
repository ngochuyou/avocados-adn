/**
 * 
 */
package adn.model.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "accounts")
public class Account extends adn.model.entities.Entity {

	@Id
	protected String id;

	@Column(nullable = false)
	protected String email;

	protected String phone;

	@Column(name = "first_name")
	protected String firstName;

	@Column(name = "last_name")
	protected String lastName;

	@Column(updatable = false)
	protected String photo;

	@Column(nullable = false)
	protected String password;

	@Enumerated(EnumType.STRING)
	protected Role role;

	@Enumerated(EnumType.STRING)
	protected Gender gender;

	@Column(name = "active", nullable = false)
	protected Boolean active;

	@Column(name = "deactivated_date")
	protected LocalDateTime deactivatedDate;

	@CreationTimestamp
	@Column(name = "created_date", nullable = false, updatable = false)
	protected LocalDateTime createdDate;

	@UpdateTimestamp
	@Column(name = "updated_date", nullable = false)
	protected LocalDateTime updatedDate;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}

	public LocalDateTime getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(LocalDateTime updatedDate) {
		this.updatedDate = updatedDate;
	}

	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public LocalDateTime getDeactivatedDate() {
		return deactivatedDate;
	}

	public void setDeactivatedDate(LocalDateTime deactivatedDate) {
		this.deactivatedDate = deactivatedDate;
	}

}
